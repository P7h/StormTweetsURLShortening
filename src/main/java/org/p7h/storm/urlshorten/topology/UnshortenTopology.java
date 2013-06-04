package org.p7h.storm.urlshorten.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import org.p7h.storm.urlshorten.bolts.UnshortenBolt;
import org.p7h.storm.urlshorten.spouts.TwitterSpout;

/**
 * Orchestrates the elements and forms a org.p7h.storm.urlshorten.urlshorten.topology to run the unshortening service.
 *
 * @author Michael Vogiatzis
 */
public final class UnshortenTopology {

	public static final void main(final String[] args) throws Exception {
		final TopologyBuilder topologyBuilder = new TopologyBuilder();

		topologyBuilder.setSpout("spout", new TwitterSpout(), 1);

		topologyBuilder.setBolt("unshortenBolt", new UnshortenBolt(), 4)
				.shuffleGrouping("spout");
		/*topologyBuilder.setBolt("dbBolt", new CassandraBolt(), 2)
	         .shuffleGrouping("unshortenBolt");*/

		final Config config = new Config();
		config.setDebug(false);

		//submit it to the cluster, or submit it locally
		if (args != null && args.length > 0) {
			config.setNumWorkers(3);

			StormSubmitter.submitTopology(args[0], config, topologyBuilder.createTopology());
		} else {
			config.setMaxTaskParallelism(10);
			final LocalCluster localCluster = new LocalCluster();
			localCluster.submitTopology("unshortening", config, topologyBuilder.createTopology());
			Thread.sleep(100000);
			localCluster.shutdown();
		}
	}
}
