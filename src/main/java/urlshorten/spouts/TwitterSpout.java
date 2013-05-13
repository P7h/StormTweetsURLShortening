package urlshorten.spouts;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import urlshorten.utils.Constants;

/**
 * Twitter spout connected to real-time stream. It stores tweet statuses to a queue
 * and emits them to the urlshorten.topology.
 *
 * @author Michael Vogiatzis
 */
public final class TwitterSpout extends BaseRichSpout {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSpout.class);

	private SpoutOutputCollector _collector;
	private LinkedBlockingQueue<Status> queue = null;

	@Override
	public final void open(final Map confMap, final TopologyContext context,
	                       final SpoutOutputCollector collector) {
		_collector = collector;
		queue = new LinkedBlockingQueue<>(1000);

		//implement a listener for twitter statuses
		final StatusListener statusListener = new StatusListener() {
			public void onStatus(Status status) {
				queue.offer(status);
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
			}

			@Override
			public void onStallWarning(StallWarning warning) {
			}
		};

		//twitter stream authentication setup
		final Properties properties = new Properties();
		try {
			properties.load(TwitterSpout.class.getClassLoader().getResourceAsStream(Constants.CONFIG_PROPERTIES_FILE));
		} catch (IOException e) {
			LOGGER.error(e.toString());
		}

		final ConfigurationBuilder twitterConf = new ConfigurationBuilder();
		twitterConf.setIncludeEntitiesEnabled(true);

		twitterConf.setOAuthAccessToken(properties.getProperty(Constants.OATH_ACCESS_TOKEN));
		twitterConf.setOAuthAccessTokenSecret(properties.getProperty(Constants.OATH_ACCESS_TOKEN_SECRET));
		twitterConf.setOAuthConsumerKey(properties.getProperty(Constants.OATH_CONSUMER_KEY));
		twitterConf.setOAuthConsumerSecret(properties.getProperty(Constants.OATH_CONSUMER_SECRET));
		final TwitterStream twitterStream = new TwitterStreamFactory(twitterConf.build()).getInstance();
		twitterStream.addListener(statusListener);

		// sample() method internally creates a thread which manipulates TwitterStream and calls
		//the listener methods continuously.
		twitterStream.sample();
	}

	@Override
	public final void nextTuple() {
		final Status status = queue.poll();
		Utils.sleep(250);
		if (status == null) {
			//if queue is empty sleep the spout thread so it doesn't consume resources
			Utils.sleep(500);
		} else {
			//LOGGER.info(status.getUser().getName() + " : " + status.getText());
			_collector.emit(new Values(status));
		}
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(Constants.TWEET));
	}


}
