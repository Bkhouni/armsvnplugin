package org.polarion.svnwebclient.util;

public class UrlGeneratorFactory {
	public static UrlGenerator getUrlGenerator(String url, String location) {
		UrlGenerator generator = new UrlGenerator(url);
		// generator.addParameter(RequestParameters.LOCATION, location);

		return generator;
	}

	public static UrlGenerator getUrlGenerator(String url) {
		UrlGenerator generator = new UrlGenerator(url);
		return generator;
	}
}
