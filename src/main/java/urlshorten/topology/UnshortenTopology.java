package urlshorten.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import urlshorten.bolts.UnshortenBolt;
import urlshorten.spouts.TwitterSpout;

/**
 * Orchestrates the elements and forms a urlshorten.topology to run the unshortening service.
 *
 * @author Michael Vogiatzis
 */
public final class UnshortenTopology {

	public static final void main(final String[] args) throws Exception {
		final TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("spout", new TwitterSpout(), 1);

		builder.setBolt("unshortenBolt", new UnshortenBolt(), 4)
				.shuffleGrouping("spout");
		/*builder.setBolt("dbBolt", new CassandraBolt(), 2)
	         .shuffleGrouping("unshortenBolt");*/

		final Config conf = new Config();
		conf.setDebug(false);

		//submit it to the cluster, or submit it locally
		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);

			StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
		} else {
			conf.setMaxTaskParallelism(10);
			final LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("unshortening", conf, builder.createTopology());
			Thread.sleep(50000);
			cluster.shutdown();
		}
	}
}
