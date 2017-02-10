package org.polarion.svnwebclient.data;

public class IncorrectParameterException extends DataProviderException {
	private static final long serialVersionUID = -8897831942817087245L;

	protected ExceptionInfo info;

	public class ExceptionInfo {
		protected String message;
		protected String description;

		public ExceptionInfo(String message, String description) {
			this.message = message;
			this.description = description;
		}

		public String getDescription() {
			return this.description;
		}

		public String getMessage() {
			return this.message;
		}
	}

	public IncorrectParameterException() {
		super();
	}

	public IncorrectParameterException(String message, String description) {
		super();
		this.info = new ExceptionInfo(message, description);
	}

	public IncorrectParameterException(String message) {
		super(message);
	}

	public IncorrectParameterException(Throwable cause) {
		super(cause);
	}

	public IncorrectParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExceptionInfo getExceptionInfo() {
		return this.info;
	}
}
