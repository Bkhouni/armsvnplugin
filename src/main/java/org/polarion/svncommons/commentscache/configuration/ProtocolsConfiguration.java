package org.polarion.svncommons.commentscache.configuration;

public class ProtocolsConfiguration {

	public static final int SVN_SSH = 1;
	public static final int SSL = 2;
	public static final int HTTP = 0;

	protected String protocolKeyFile;
	protected String protocolPassPhrase;
	protected int protocolPortNumber;
	protected int protocolType;
	protected ProxySettings proxy = new ProxySettings();

	public ProtocolsConfiguration() {
	}

	public ProxySettings getProxy() {
		return this.proxy;
	}

	public void setProxy(ProxySettings proxy) {
		this.proxy = proxy;
	}

	public String getProtocolKeyFile() {
		return this.protocolKeyFile;
	}

	public void setProtocolKeyFile(String protocolKeyFile) {
		this.protocolKeyFile = protocolKeyFile;
	}

	public String getProtocolPassPhrase() {
		return this.protocolPassPhrase;
	}

	public void setProtocolPassPhrase(String protocolPassPhrase) {
		this.protocolPassPhrase = protocolPassPhrase;
	}

	public int getProtocolPortNumber() {
		return this.protocolPortNumber;
	}

	public void setProtocolPortNumber(int protocolPortNumber) {
		this.protocolPortNumber = protocolPortNumber;
	}

	public int getProtocolType() {
		return this.protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}
}
