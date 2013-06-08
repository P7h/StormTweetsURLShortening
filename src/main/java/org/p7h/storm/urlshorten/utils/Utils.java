package org.p7h.storm.urlshorten.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.google.common.collect.Lists;
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
		final List<UnShortMe> urlInfo = Lists.newArrayList();
		final ObjectMapper objectMapper = new ObjectMapper();
		String requestedUrl = null;
		StringBuilder constructedURL = new StringBuilder();
		UnShortMe unShortMe;
		for (int i = 0; i < urls.length; i++) {
			constructedURL.setLength(0);
			requestedUrl = urls[i].getURL();
			constructedURL.append(Constants.API_UNSHORT_ME)
					.append(requestedUrl)
					.append("&t=json");
			unShortMe = resolveURLs(objectMapper, constructedURL);
			urlInfo.add(unShortMe);
		}

		return urlInfo;
	}

	private static final UnShortMe resolveURLs(final ObjectMapper objectMapper,
	                                     final StringBuilder constructedURL) {
		final URL url;
		final URLConnection conn;
		UnShortMe unShortMe = new UnShortMe();

		try {
			url = new URL(constructedURL.toString());
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
			LOGGER.error(exception.toString());
		}
		return unShortMe;
	}
}