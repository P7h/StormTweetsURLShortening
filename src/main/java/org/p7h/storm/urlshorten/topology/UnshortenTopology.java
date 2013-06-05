package org.p7h.storm.urlshorten.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import org.p7h.storm.urlshorten.bolts.UnshortenBolt;
import org.p7h.storm.urlshorten.spouts.TwitterSpout;

/**
 * Orchestrates the elements and forms a Topology to run the unshortening service.
 *
 * @author Michael Vogiatzis
 */
public final class UnshortenTopology {
	private static final String TOPOLOGY_NAME = "UnshortenURLs";

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
			config.setMessageTimeoutSecs(120);
			final LocalCluster localCluster = new LocalCluster();
			localCluster.submitTopology(TOPOLOGY_NAME, config, topologyBuilder.createTopology());
			//Sleep for 100 seconds
			Thread.sleep(100 * 1000);

			//Create a ShutdownHook for JVM so that we can kill and shutdown the Cluster gracefully.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public final void run() {
					localCluster.killTopology(TOPOLOGY_NAME);
					localCluster.shutdown();
				}
			});
		}
	}
}
