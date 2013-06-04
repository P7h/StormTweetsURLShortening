package org.p7h.storm.urlshorten.domain;

import com.google.common.base.Objects;

/**
 * POJO class for JSON reponse provided by Unshort.Me API.
 * User: Prashanth Babu
 * Date: 5/13/13
 * Time: 12:36 AM
 */
public final class UnShortMe {
	private String requestedURL;
	private boolean success;
	private String resolvedURL;

	public final String getRequestedURL() {
		return requestedURL;
	}

	public final void setRequestedURL(final String requestedURL) {
		this.requestedURL = requestedURL;
	}

	public final boolean isSuccess() {
		return success;
	}

	public final void setSuccess(final boolean success) {
		this.success = success;
	}

	public final String getResolvedURL() {
		return resolvedURL;
	}

	public final void setResolvedURL(final String resolvedURL) {
		this.resolvedURL = resolvedURL;
	}

	/**
	 * Guava API helper generated toString() method for a Java Bean / POJO.
	 *
	 * @return String representation of the attributes of the bean.
	 */
	@Override
	public final String toString() {
		return Objects.toStringHelper(this)
				       .add("requestedURL", requestedURL)
				       .add("success", success)
				       .add("resolvedURL", resolvedURL)
				       .toString();
	}
}
