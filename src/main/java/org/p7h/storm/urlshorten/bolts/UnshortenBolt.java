package org.p7h.storm.urlshorten.bolts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.p7h.storm.urlshorten.domain.UnShortMe;
import org.p7h.storm.urlshorten.utils.Constants;
import org.p7h.storm.urlshorten.utils.UnShortMeAPILookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Gets a Tweet status and emits shortened urls along with their expansion.
 *
 * @author Michael Vogiatzis
 * @author Prashanth Babu
 */
public final class UnshortenBolt extends BaseRichBolt {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnshortenBolt.class);
	private static final long serialVersionUID = -2645746161268111353L;
	private OutputCollector _outputCollector;

	@Override
	public final void prepare(final Map stormConf, final TopologyContext context,
	                          final OutputCollector outputCollector) {
		this._outputCollector = outputCollector;
	}

	@Override
	public final void execute(final Tuple tuple) {
		final Status status = (Status) tuple.getValueByField(Constants.TWEET);
		final URLEntity[] urlEntities = status.getURLEntities();
		final List<UnShortMe> urlInfo = UnShortMeAPILookup.unshortenIt(urlEntities);
		UnShortMe unShortMe;
		String requestedURL;
		String resolvedURL;
		for (final Iterator<UnShortMe > iterator = urlInfo.iterator(); iterator.hasNext();) {
			unShortMe =  iterator.next();
			requestedURL = unShortMe.getRequestedURL();
			resolvedURL = unShortMe.getResolvedURL();
			if (null != requestedURL && null != resolvedURL) {
				LOGGER.info("\t{} ==> {}", requestedURL, resolvedURL);
				//_outputCollector.emit(new Values(requestedURL, resolvedURL));
			}
		}
		_outputCollector.ack(tuple);
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
//		declarer.declare(new Fields(Constants.SHORT_URL, Constants.EXPANDED_URL));
	}
}