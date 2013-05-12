package urlshorten.bolts;

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
		final Status status = (Status) tuple.getValueByField("tweet");
		final URLEntity[] urls = status.getURLEntities();
		/*
		String shortURL;
		String expandedURL;
		for (URLEntity url : urls) {
			shortURL = url.getURL();
			expandedURL = Utils.unshortenIt(shortURL);
			if (expandedURL != null) {//ouc.emit(new Values(shortURL, expandedURL));
				LOGGER.info("Emitting: " + shortURL + ", " + expandedURL);
			}
		}*/
		final List<UnShortMe> urlInfo = Utils.unshortenIt1(urls);
		UnShortMe unShortMe = null;
		String requestedURL = null;
		String resolvedURL = null;

		for (int i = 0; i < urlInfo.size(); i++) {
			unShortMe = urlInfo.get(i);
			requestedURL = unShortMe.getRequestedURL();
			resolvedURL = unShortMe.getResolvedURL();
			if (null != requestedURL && null != resolvedURL) {
				//ouc.emit(new Values(unShortMe.getRequestedURL(), unShortMe.getResolvedURL()));
				LOGGER.info("Emitting: " + requestedURL + ", " + resolvedURL);
			}
		}
		ouc.ack(tuple);
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
		//declarer.declare(new Fields("shortUrl", "expUrl"));
	}
}
