package org.p7h.storm.urlshorten.spouts;

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
import org.p7h.storm.urlshorten.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter spout connected to real-time stream. It stores tweet statuses to a _queue
 * and emits them to the Topology.
 *
 * @author Michael Vogiatzis
 */
public final class TwitterSpout extends BaseRichSpout {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSpout.class);
	private static final long serialVersionUID = 3189251362566074681L;

	private SpoutOutputCollector _collector = null;
	private LinkedBlockingQueue<Status> _queue = null;
	private TwitterStream _twitterStream = null;

	@Override
	public final void open(final Map confMap, final TopologyContext context,
	                       final SpoutOutputCollector collector) {
		this._collector = collector;
		this._queue = new LinkedBlockingQueue<>(1000);

		final StatusListener statusListener = new StatusListener() {
			public void onStatus(Status status) {
				_queue.offer(status);
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

		//Twitter stream authentication setup
		final Properties properties = new Properties();
		try {
			properties.load(TwitterSpout.class.getClassLoader()
					                .getResourceAsStream(Constants.CONFIG_PROPERTIES_FILE));
		} catch (final IOException exception) {
			//Should not occur. If it does, we cant continue. So exiting the program!
			LOGGER.error(exception.toString());
			System.exit(1);
		}

		//Twitter stream authentication setup
		final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setIncludeEntitiesEnabled(true);

		configurationBuilder.setOAuthAccessToken(properties.getProperty(Constants.OATH_ACCESS_TOKEN));
		configurationBuilder.setOAuthAccessTokenSecret(properties.getProperty(Constants.OATH_ACCESS_TOKEN_SECRET));
		configurationBuilder.setOAuthConsumerKey(properties.getProperty(Constants.OATH_CONSUMER_KEY));
		configurationBuilder.setOAuthConsumerSecret(properties.getProperty(Constants.OATH_CONSUMER_SECRET));
		_twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
		_twitterStream.addListener(statusListener);

		//Returns a small random sample of all public statuses.
		_twitterStream.sample();
	}

	@Override
	public final void close() {
		_twitterStream.shutdown();
	}

	@Override
	public final void nextTuple() {
		final Status status = _queue.poll();
		if (null == status) {
			//If _queue is empty sleep the spout thread so it doesn't consume resources.
			Utils.sleep(500);
		} else {
			_collector.emit(new Values(status));
		}
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(Constants.TWEET));
	}
}