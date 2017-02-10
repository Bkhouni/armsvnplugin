package org.polarion.svncommons.commentscache.authentication;

import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import com.opensymphony.util.TextUtils;
import org.polarion.svncommons.commentscache.configuration.ProtocolsConfiguration;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.*;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javax.net.ssl.TrustManager;
import java.io.File;

public class SVNAuthenticationManager implements ISVNAuthenticationManager {

	protected String username;
	protected String password;

	protected ProtocolsConfiguration protocols;

	protected ISVNAuthenticationManager defaultManager;

	public SVNAuthenticationManager(String username, String password,
			ProtocolsConfiguration protocols) {
		this.username = username;
		this.password = password;
		this.protocols = protocols;

		this.defaultManager = SVNWCUtil.createDefaultAuthenticationManager(
				this.username, this.password);
	}

	public SVNAuthentication getFirstAuthentication(String kind, String realm,
			SVNURL url) throws SVNException {
		if (ISVNAuthenticationManager.SSH.equals(kind)) {
			if (TextUtils.stringSet(this.protocols.getProtocolKeyFile())) {
				return new SVNSSHAuthentication(this.username, new File(
						this.protocols.getProtocolKeyFile()),
						this.protocols.getProtocolPassPhrase(),
						this.protocols.getProtocolPortNumber(), false);
			} else {
				return new SVNSSHAuthentication(this.username, this.password,
						this.protocols.getProtocolPortNumber(), false);
			}
		} else if (ISVNAuthenticationManager.SSL.equals(kind)) {
			return new SVNSSLAuthentication(new File(
					this.protocols.getProtocolKeyFile()),
					this.protocols.getProtocolPassPhrase(), false);
		} else if (ISVNAuthenticationManager.PASSWORD.equals(kind)) {
			return new SVNPasswordAuthentication(this.username, this.password,
					false);
		} else if (ISVNAuthenticationManager.USERNAME.equals(kind)) {
			return new SVNUserNameAuthentication(this.username, false);
		} else {
			return null;
		}
	}

	public int getConnectTimeout(SVNRepository repository) {
		return PluginConnectionPool.getSVNConnectionTimeout();
	}

	public int getReadTimeout(SVNRepository repository) {
		return PluginConnectionPool.getSVNReadTimeout();
	}

	public SVNAuthentication getNextAuthentication(String kind, String realm,
			SVNURL url) throws SVNException {
		throw new SVNAuthenticationException(
				SVNErrorMessage.create(SVNErrorCode.AUTHN_CREDS_UNAVAILABLE));
	}

	public ISVNProxyManager getProxyManager(SVNURL url) throws SVNException {
		if (this.protocols != null) {
			return new SVNProxyManager(this.protocols.getProxy());
		} else {
			return null;
		}
	}

	public void acknowledgeTrustManager(TrustManager manager) {
		defaultManager.acknowledgeTrustManager(manager);
	}

	public TrustManager getTrustManager(SVNURL url) throws SVNException {
		return defaultManager.getTrustManager(url);
	}

	public boolean isAuthenticationForced() {
		return false;
	}

	public void setAuthenticationProvider(ISVNAuthenticationProvider provider) {
	}

	public void acknowledgeAuthentication(boolean accepted, String kind,
			String realm, SVNErrorMessage errorMessage,
			SVNAuthentication authentication) throws SVNException {
	}

}
