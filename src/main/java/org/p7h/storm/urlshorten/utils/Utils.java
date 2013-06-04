package org.p7h.storm.urlshorten.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.p7h.storm.urlshorten.domain.UnShortMe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.URLEntity;

public final class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	/**
	 * Method which hits Unshort.Me thru API request and gets and parses the JSON response.
	 * @param urls -- URLEntities
	 * @return List of UnShortMe Objects.
	 */
	public static List<UnShortMe> unshortenIt(final URLEntity[] urls) {

		URL url = null;
		URLConnection conn = null;
		UnShortMe unShortMe = new UnShortMe();
		final List<UnShortMe> urlInfo = new ArrayList<>();
		final ObjectMapper mapper = new ObjectMapper();
		String requestedUrl = null;
		StringBuilder constructedURL = new StringBuilder();
		for (int i = 0; i < urls.length; i++) {
			constructedURL.setLength(0);
			requestedUrl = urls[i].getURL();
			constructedURL.append("http://api.unshort.me/?r=")
					.append(requestedUrl)
					.append("&t=json");
			try {
				url = new URL(constructedURL.toString());
				//open URL connection
				conn = url.openConnection();

				//unshort.me returns an XML if invalid url is passed as an input
				unShortMe = mapper.readValue(conn.getInputStream(), UnShortMe.class);
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
				LOGGER.error(exception.toString());
			}
			urlInfo.add(unShortMe);
		}

		return urlInfo;
	}
}

