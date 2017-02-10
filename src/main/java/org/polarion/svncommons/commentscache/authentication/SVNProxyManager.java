package org.polarion.svncommons.commentscache.authentication;

import org.polarion.svncommons.commentscache.configuration.ProxySettings;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.auth.ISVNProxyManager;

public class SVNProxyManager implements ISVNProxyManager {

	protected ProxySettings proxySettings;

	public SVNProxyManager(ProxySettings proxySettings) {
		this.proxySettings = proxySettings;
	}

	public void acknowledgeProxyContext(boolean accepted,
			SVNErrorMessage errorMessage) {
	}

	public String getProxyHost() {
		return this.proxySettings.getHost();
	}

	public String getProxyPassword() {
		return this.proxySettings.getPassword();
	}

	public int getProxyPort() {
		return this.proxySettings.getPort();
	}

	public String getProxyUserName() {
		return this.proxySettings.getUserName();
	}
}
