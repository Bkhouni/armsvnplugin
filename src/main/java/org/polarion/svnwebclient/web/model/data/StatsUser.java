package org.polarion.svnwebclient.web.model.data;

import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.RequestParameters;

public class StatsUser {
	public static String getUrl(String username) {
		UrlGenerator urlGenerator = UrlGeneratorFactory
				.getUrlGenerator(Links.STATS_USER);
		urlGenerator.addParameter(RequestParameters.USERNAME,
				UrlUtil.encode(username));
		return urlGenerator.getUrl();
	}
}
