package org.polarion.svnwebclient.configuration;

public class ConfigurationError {
	protected boolean isError = false;
	protected Exception exception;

	public boolean isError() {
		return this.isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Exception getException() {
		return this.exception;
	}
}
