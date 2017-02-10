package org.polarion.svnwebclient.data.model;

public class DataFileElement extends DataRepositoryElement {
	protected boolean binary;
	protected byte[] content;

	public boolean isDirectory() {
		return false;
	}

	public boolean isBinary() {
		return this.binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public byte[] getContent() {
		return this.content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
}
