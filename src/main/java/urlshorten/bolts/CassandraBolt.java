package urlshorten.bolts;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.FailoverPolicy;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populates the urlshorten.cassandra tables.
 *
 * @author Michael Vogiatzis
 */
public final class CassandraBolt extends BaseRichBolt {
	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraBolt.class);
	private static Cluster cluster;
	private static Keyspace keyspace;
	private static Mutator<String> mutator;
	private OutputCollector ouc;

	@Override
	public final void prepare(final Map stormConf, final TopologyContext context,
	                          final OutputCollector collector) {
		ouc = collector;
		// load from properties file
		final Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("config.properties"));
		} catch (IOException ex) {
			LOGGER.error(ex.toString());
		}

		//urlshorten.cassandra configuration
		cluster = HFactory.getOrCreateCluster(
				                                     prop.getProperty("CLUSTERNAME"), prop.getProperty("HOST"));
		final ConfigurableConsistencyLevel ccl = new ConfigurableConsistencyLevel();
		ccl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
		keyspace = HFactory.createKeyspace("links", cluster, ccl,
				                                  FailoverPolicy.FAIL_FAST);
		mutator = HFactory.createMutator(keyspace, StringSerializer.get());
	}

	@Override
	public final void execute(final Tuple tuple) {
		final String expUrl = tuple.getStringByField("expUrl");

		String shortUrl;
		if (expUrl != null) {
			shortUrl = tuple.getStringByField("shortUrl");
			updateDB(shortUrl, expUrl);
			LOGGER.info("DB Update!");
		}

		ouc.ack(tuple);
	}


	/**
	 * Updates the urlshorten.cassandra table links with unshortened urls.
	 *
	 * @param shortUrl
	 * @param expandedUrl
	 */
	public final void updateDB(final String shortUrl, final String expandedUrl) {
		mutator.addInsertion(shortUrl, "l", HFactory.createColumn("u", expandedUrl));
		mutator.execute();
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer declarer) {
	}

}
