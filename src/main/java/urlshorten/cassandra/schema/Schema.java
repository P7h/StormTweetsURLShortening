package urlshorten.cassandra.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import urlshorten.utils.Constants;

/**
 * Database schema builds automatically if the keyspaces do not exist.
 *
 * @author Michael Vogiatzis
 */
public final class Schema {

	private static int replFactor;
	static String host;
	static String clusterName;
	static Cluster cluster;

	/**
	 * @param args
	 */
	public static final void main(final String[] args) {

		final Properties prop = new Properties();
		replFactor = 1;
		try {
			prop.load(Schema.class.getClassLoader().getResourceAsStream(Constants.CONFIG_PROPERTIES_FILE));
			replFactor = Integer.valueOf(prop.getProperty(Constants.REPL_FACTOR));
			host = String.valueOf(prop.getProperty(Constants.HOST));
			clusterName = String.valueOf(prop.getProperty(Constants.CLUSTERNAME));
			System.out.println("Replication factor: " + replFactor);
			System.out.println("Host: " + host);
			System.out.println("Cluster name: " + clusterName);

		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		cluster = HFactory.getOrCreateCluster(clusterName, host);

		KeyspaceDefinition keyspaceDef = cluster.describeKeyspace(Constants.TWEETS);

		if (keyspaceDef == null) {
			createTweetsTable();
		}

		keyspaceDef = cluster.describeKeyspace(Constants.LINKS);

		if (keyspaceDef == null) {
			createLinksTable();
		}
	}

	private static void createTweetsTable() {

		final String QF_ID = "id";

		// Column Family tweets
		final String CF_TWEET = "t";
		final String QF_TEXT = "txt";

		final ColumnFamilyDefinition cfTweet = HFactory.createColumnFamilyDefinition(Constants.TWEETS,
					CF_TWEET, ComparatorType.UTF8TYPE);
		cfTweet.setKeyValidationClass(ComparatorType.LONGTYPE.getTypeName());

		final List<ColumnDefinition> tweetMetaData = new ArrayList<ColumnDefinition>();

		final BasicColumnDefinition t_txt = new BasicColumnDefinition();
		t_txt.setName(StringSerializer.get().toByteBuffer(QF_TEXT));
		t_txt.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		tweetMetaData.add(t_txt);
		cfTweet.addColumnDefinition(t_txt);

		final List<ColumnFamilyDefinition> cfDefs = new ArrayList<ColumnFamilyDefinition>();
		cfDefs.add(cfTweet);

		final KeyspaceDefinition tweetsDef = HFactory.createKeyspaceDefinition(Constants. TWEETS,
					Constants.SIMPLESTRATEGY, replFactor, cfDefs);

		cluster.addKeyspace(tweetsDef);
	}

	private static final void createLinksTable() {
		// column family l
		final String CF_LINKS = "l";
		final String QF_URL = "u";

		final ColumnFamilyDefinition cfLinks = HFactory.createColumnFamilyDefinition(Constants.LINKS,
					CF_LINKS, ComparatorType.UTF8TYPE);
		cfLinks.setKeyValidationClass(ComparatorType.UTF8TYPE.getTypeName());

		final List<ColumnDefinition> linksMetaData = new ArrayList<>();

		final BasicColumnDefinition twLinks = new BasicColumnDefinition();
		twLinks.setName(StringSerializer.get().toByteBuffer(QF_URL));
		twLinks.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		linksMetaData.add(twLinks);
		cfLinks.addColumnDefinition(twLinks);

		final List<ColumnFamilyDefinition> cfDefs = new ArrayList<ColumnFamilyDefinition>();
		cfDefs.add(cfLinks);

		final KeyspaceDefinition linksDef = HFactory.createKeyspaceDefinition(Constants.LINKS,
					Constants.SIMPLESTRATEGY, replFactor, cfDefs);

		cluster.addKeyspace(linksDef);
	}
}
