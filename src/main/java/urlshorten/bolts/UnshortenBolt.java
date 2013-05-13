package urlshorten.bolts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;
import urlshorten.domain.UnShortMe;
import urlshorten.utils.Constants;
import urlshorten.utils.Utils;

/**
 * Gets a Tweet status and emits shortened urls along with their expansion if any.
 *
 * @author Michael Vogiatzis
 */
public final class UnshortenBolt extends BaseRichBolt {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnshortenBolt.class);
	private OutputCollector ouc;

	@Override
	public final void prepare(final Map stormConf, final TopologyContext context,
	                          final OutputCollector collector) {
		ouc = collector;
	}

	@Override
	public final void execute(final Tuple tuple) {
		final Status status = (Status) tuple.getValueByField(Constants.TWEET);
		final URLEntity[] urls = status.getURLEntities();
		final List<UnShortMe> urlInfo = Utils.unshortenIt(urls);
		UnShortMe unShortMe = null;
		String requestedURL = null;
		String resolvedURL = null;
		for (Iterator<UnShortMe> iterator = urlInfo.iterator(); iterator.hasNext(); ) {
			unShortMe =  iterator.next();
			requestedURL = unShortMe.getRequestedURL();
			resolvedURL = unShortMe.getResolvedURL();
			if (null != requestedURL && null != resolvedURL) {
				LOGGER.info("Emitting: " + requestedURL + ", " + resolvedURL);
				//ouc.emit(new Values(requestedURL, resolvedURL));
			}
		}

		ouc.ack(tuple);
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
//		declarer.declare(new Fields(Constants.SHORT_URL, Constants.EXPANDED_URL));
	}
}
