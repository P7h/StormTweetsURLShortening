package urlshorten.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.URLEntity;
import urlshorten.domain.UnShortMe;

public final class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	public final static String unshortenIt(final String requestedUrl) {

		UnShortMe unShortMe = new UnShortMe();
		try {
			final URL url = new URL("http://api.unshort.me/?r=" + requestedUrl + "&t=json");
			//open URL connection
			final URLConnection conn = url.openConnection();

			//unshort.me returns an XML if invalid url is passed as an input
			final ObjectMapper mapper = new ObjectMapper();
			unShortMe = mapper.readValue(conn.getInputStream(), UnShortMe.class);
			if (!unShortMe.isSuccess()) {
				return null;
			}
		} catch (final JsonParseException jpe) {
			//unshort.me returns XML format in case of invalid URL, and it happens often.
			return null;
		} catch (final MalformedURLException e) {
			unShortMe.setResolvedURL(null);
			LOGGER.error(e.toString());
		} catch (final IOException exception) {
			unShortMe.setResolvedURL(null);
			//usually Http Response code 503
			LOGGER.error(exception.toString());
		}

		return unShortMe.getResolvedURL();
	}

	public static List<UnShortMe> unshortenIt1(final URLEntity[] urls) {

		URL url = null;
		UnShortMe unShortMe = new UnShortMe();
		URLConnection conn = null;
		final List<UnShortMe> urlInfo = new ArrayList<>();
		final ObjectMapper mapper = new ObjectMapper();
		for (int i = 0; i < urls.length; i++) {
			String requestedUrl = urls[i].getURL();
			try {
				url = new URL("http://api.unshort.me/?r=" + requestedUrl + "&t=json");
				//open URL connection
				conn = url.openConnection();

				//unshort.me returns an XML if invalid url is passed as an input
				unShortMe = mapper.readValue(conn.getInputStream(), UnShortMe.class);
				if (!unShortMe.isSuccess()) {
					unShortMe.setResolvedURL(null);
				}
			} catch (final JsonParseException exception) {
				unShortMe.setResolvedURL(null);
				//unshort.me returns XML format in case of invalid URL, and it happens often.
				LOGGER.error(exception.toString());
			} catch (final MalformedURLException exception) {
				unShortMe.setResolvedURL(null);
				LOGGER.error(exception.toString());
			} catch (final IOException exception) {
				unShortMe.setResolvedURL(null);
				//usually Http Response code 503
				LOGGER.error(exception.toString());
			}
			urlInfo.add(unShortMe);
		}

		return urlInfo;
	}
}

