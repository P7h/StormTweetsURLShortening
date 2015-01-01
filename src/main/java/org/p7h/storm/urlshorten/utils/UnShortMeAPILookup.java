package org.p7h.storm.urlshorten.utils;

import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.p7h.storm.urlshorten.domain.UnShortMe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.URLEntity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

/**
 * Utility class which looks up UnshortMe API with the requested short URL for Unshorten / resolved URL.
 *
 * @author Prashanth Babu
 */
public final class UnShortMeAPILookup {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnShortMeAPILookup.class);
	private static final String UNSHORT_ME_API_KEY;

	static {
		//For reading UnShortMe API Key.
		final Properties properties = new Properties();
		try {
			properties.load(UnShortMeAPILookup.class.getClassLoader()
					.getResourceAsStream(Constants.CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			//Should not occur. If it does, we cant continue. So exiting the program!
			LOGGER.error(ioException.getMessage(), ioException);
			System.exit(1);
		}
		UNSHORT_ME_API_KEY = properties.getProperty(Constants.UNSHORT_ME_API_KEY);
	}


	/**
	 * Hits Unshort.Me thru API request and gets and parses the JSON response.
	 *
	 * @param urlEntities -- URLEntities
	 * @return List of UnShortMe Objects.
	 */
	public static List<UnShortMe> unshortenIt(final URLEntity[] urlEntities) {

		final List<UnShortMe> urlInfo = Lists.newArrayList();
		final ObjectMapper objectMapper = new ObjectMapper();
		final StringBuilder constructedURL = new StringBuilder();
		String requestedUrl;
		UnShortMe unShortMe;
		for (int i = 0; i < urlEntities.length; i++) {
			constructedURL.setLength(0);
			requestedUrl = urlEntities[i].getURL();
			constructedURL.append(Constants.API_UNSHORT_ME)
					.append(requestedUrl)
					.append("&api_key=" + UNSHORT_ME_API_KEY)
					.append("&format=json");
			unShortMe = resolveURLsFromJSON(objectMapper, constructedURL.toString());
			urlInfo.add(unShortMe);
		}

		return urlInfo;
	}

	/**
	 * Unmarshall the JSON Response to a POJO for unshortening the URLs.
	 *
	 * @param objectMapper ObjectMapper
	 * @param constructedURL UnShortMe API URL we have to access
	 * @return unmarshalled POJO
	 */
	private static final UnShortMe resolveURLsFromJSON(final ObjectMapper objectMapper,
	                                                   final String constructedURL) {
		final URL url;
		final URLConnection conn;
		UnShortMe unShortMe = new UnShortMe();

		try {
			url = new URL(constructedURL);
			//open URL connection
			conn = url.openConnection();

			//unshort.me returns an XML if invalid url is passed as an input
			unShortMe = objectMapper.readValue(conn.getInputStream(), UnShortMe.class);
			if (!unShortMe.isSuccess()) {
				unShortMe.setResolvedURL(null);
			}
		} catch (final JsonParseException | MalformedURLException exception) {
			unShortMe.setResolvedURL(null);
			//unshort.me returns XML format in case of invalid URL, and it happens often.
			LOGGER.error(exception.toString());
		} catch (final Exception exception) {
			unShortMe.setResolvedURL(null);
			//usually Http Response code 503
			exception.printStackTrace();
			LOGGER.error(exception.toString());
		}
		return unShortMe;
	}
}