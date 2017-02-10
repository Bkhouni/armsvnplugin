package org.polarion.svnwebclient.web.controller;

import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestException;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatsUserBean extends AbstractBean {

	protected String username;
	protected IDataProvider dataProvider;

	@Override
	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.username = this.requestHandler.getUsername();
		return true;
	}

	@Override
	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(), "",
				this.requestHandler.getLocation(), -1);
	}

	@Override
	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	@Override
	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkNotNullOrEmpty(RequestParameters.USERNAME);
			}
		};
	}

	public long getRepoId() throws SQLException {
		return SWCUtils.getConfigurationProvider(dataProvider.getId())
				.getRepoId();
	}

	public String getUsername() {
		return username;
	}

}
