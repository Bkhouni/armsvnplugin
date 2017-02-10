package org.polarion.svnwebclient.web.controller;

import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.util.NumberFormatter;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.Button;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommitGraphBean extends AbstractBean {

	protected long headRevision;
	protected long revision;
	protected String url;
	protected DataRepositoryElement repoElement;
	protected IDataProvider dataProvider;

	@Override
	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		if (this.requestHandler.getPegRevision() != -1) {
			this.revision = this.requestHandler.getPegRevision();
		}
		this.url = this.requestHandler.getUrl();

		this.repoElement = dataProvider.getInfo(this.url, this.revision);
		if (repoElement.isDirectory()) {
			repoElement = dataProvider.getDirectory(url, this.revision);
		}

		return true;
	}

	@Override
	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(), this.url,
				this.requestHandler.getLocation(),
				this.requestHandler.getPegRevision(),
				!repoElement.isDirectory());
	}

	public boolean isDirectory() {
		return repoElement.isDirectory();
	}

	@Override
	public List getActions() {
		List ret = new ArrayList();

		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.STATS_ITEM, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		urlGenerator.addParameter(RequestParameters.PEGREV,
				Long.toString(revision));
		ret.add(new Button(urlGenerator.getUrl(), Images.STATISTICS,
				"Statistics"));

		return ret;
	}

	@Override
	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkNotNullOrEmpty(RequestParameters.URL);
			}
		};
	}

	public String getMessage() {
		return repoElement.getName();
	}

	public DataRepositoryElement getRepoElement() {
		return repoElement;
	}

	public long getRepoId() throws SQLException {
		return SWCUtils.getConfigurationProvider(dataProvider.getId())
				.getRepoId();
	}

	public String getRevision() {
		return Long.toString(this.revision);
	}

	public String getRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.url;
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, this.getRevision());
		return urlGenerator.getUrl();
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.revision);
	}

	public boolean isHeadRevision() {
		return this.headRevision == this.repoElement.getRevision();
	}

}
