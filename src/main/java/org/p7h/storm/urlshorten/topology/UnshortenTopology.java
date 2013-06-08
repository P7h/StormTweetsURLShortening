package org.p7h.storm.urlshorten.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import org.p7h.storm.urlshorten.bolts.UnshortenBolt;
import org.p7h.storm.urlshorten.spouts.TwitterSpout;
import org.p7h.storm.urlshorten.utils.Constants;

/**
 * Orchestrates the elements and forms a Topology to run the unshortening service.
 *
 * @author Michael Vogiatzis
 */
public final class UnshortenTopology {

	public static final void main(final String[] args) throws Exception {
		final TopologyBuilder topologyBuilder = new TopologyBuilder();

		topologyBuilder.setSpout("twitterspout", new TwitterSpout(), 1);

		topologyBuilder.setBolt("unshortenbolt", new UnshortenBolt(), 4)
				.shuffleGrouping("twitterspout");
		/*topologyBuilder.setBolt("dbBolt", new CassandraBolt(), 2)
	         .shuffleGrouping("unshortenBolt");*/

		final Config config = new Config();
		config.setMessageTimeoutSecs(120);
		config.setDebug(false);

		//Submit it to the cluster, or submit it locally
		if (null != args && 0 < args.length) {
			config.setNumWorkers(3);

			StormSubmitter.submitTopology(args[0], config, topologyBuilder.createTopology());
		} else {
			config.setMaxTaskParallelism(10);
			final LocalCluster localCluster = new LocalCluster();
			localCluster.submitTopology(Constants.TOPOLOGY_NAME, config, topologyBuilder.createTopology());
			//Run this topology for 120 seconds so that we can complete processing of decent # of tweets.
			Utils.sleep(120 * 1000);

			//Create a ShutdownHook for JVM so that we can kill and shutdown the Cluster gracefully.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public final void run() {
					localCluster.killTopology(Constants.TOPOLOGY_NAME);
					localCluster.shutdown();
				}
			});
		}
	}
}