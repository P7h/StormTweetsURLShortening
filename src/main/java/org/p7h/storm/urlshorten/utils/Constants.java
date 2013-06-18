package org.p7h.storm.urlshorten.utils;

/**
 * Constants used in this project.
 * @author  Prashanth Babu
 */
public final class Constants {
	public static final String TOPOLOGY_NAME = "UnshortenURLs";
	public static final String CONFIG_PROPERTIES_FILE = "config.properties";
	public static final String OAUTH_ACCESS_TOKEN = "OAUTH_ACCESS_TOKEN";
	public static final String OAUTH_ACCESS_TOKEN_SECRET = "OAUTH_ACCESS_TOKEN_SECRET";
	public static final String OAUTH_CONSUMER_KEY = "OAUTH_CONSUMER_KEY";
	public static final String OAUTH_CONSUMER_SECRET = "OAUTH_CONSUMER_SECRET";
	public static final String TWEET = "tweet";
	public static final String SHORT_URL = "shortUrl";
	public static final String EXPANDED_URL = "expUrl";
	public static final String CLUSTERNAME = "CLUSTERNAME";
	public static final String HOST = "HOST";
	public static final String REPL_FACTOR = "REPL_FACTOR";
	public static final String TWEETS_CASSANDRA = "tw";
	public static final String LINKS = "links";
	public static final String SIMPLESTRATEGY = "org.apache.org.p7h.storm.urlshorten.cassandra.locator.SimpleStrategy";

	public static final String API_UNSHORT_ME = "http://api.unshort.me/?r=";
}
