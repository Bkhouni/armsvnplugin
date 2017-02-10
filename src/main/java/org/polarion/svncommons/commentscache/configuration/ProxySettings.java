package org.polarion.svncommons.commentscache.configuration;

public class ProxySettings {
	protected boolean isProxySupported;
	protected String host;
	protected int port;
	protected String userName;
	protected String password;

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isProxySupported() {
		return this.isProxySupported;
	}

	public void setProxySupported(boolean isProxySupported) {
		this.isProxySupported = isProxySupported;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
