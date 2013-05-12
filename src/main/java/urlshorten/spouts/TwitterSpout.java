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

/**
 * Twitter spout connected to real-time stream. It stores tweet statuses to a queue
 * and emits them to the urlshorten.topology.
 *
 * @author Michael Vogiatzis
 */
public final class TwitterSpout extends BaseRichSpout {

	private static final Logger log = LoggerFactory.getLogger(TwitterSpout.class);

	private SpoutOutputCollector _collector;
	private LinkedBlockingQueue<Status> queue = null;

	@Override
	public final void open(final Map confMap, final TopologyContext context,
	                       final SpoutOutputCollector collector) {
		_collector = collector;
		queue = new LinkedBlockingQueue<>(1000);

		//implement a listener for twitter statuses
		final StatusListener listener = new StatusListener() {
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
		final Properties prop = new Properties();
		try {
			prop.load(TwitterSpout.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			log.error(e.toString());
		}

		final ConfigurationBuilder twitterConf = new ConfigurationBuilder();
		twitterConf.setIncludeEntitiesEnabled(true);

		twitterConf.setOAuthAccessToken(prop.getProperty("OATH_ACCESS_TOKEN"));
		twitterConf.setOAuthAccessTokenSecret(prop.getProperty("OATH_ACCESS_TOKEN_SECRET"));
		twitterConf.setOAuthConsumerKey(prop.getProperty("OATH_CONSUMER_KEY"));
		twitterConf.setOAuthConsumerSecret(prop.getProperty("OATH_CONSUMER_SECRET"));
		final TwitterStream twitterStream = new TwitterStreamFactory(twitterConf.build()).getInstance();
		twitterStream.addListener(listener);

		// sample() method internally creates a thread which manipulates TwitterStream and calls
		//the listener methods continuously.
		twitterStream.sample();
	}

	@Override
	public final void nextTuple() {
		final Status ret = queue.poll();
		Utils.sleep(200);
		if (ret == null) {
			//if queue is empty sleep the spout thread so it doesn't consume resources
			Utils.sleep(50);
		} else {
			_collector.emit(new Values(ret));
			//log.info(ret.getUser().getName() + " : " + ret.getText());
		}
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tweet"));
	}


}
