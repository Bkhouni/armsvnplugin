package org.polarion.svnwebclient.web;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class GZIPResponseWrapper extends HttpServletResponseWrapper {

	protected GZIPResponseStream stream;
	protected PrintWriter writer;
	protected HttpServletResponse response;

	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
		this.response = response;
	}

	protected GZIPResponseStream createOutputStream() throws IOException {
		return new GZIPResponseStream(this.response);
	}

	public void finishResponse() {
		try {
			if (this.writer != null) {
				this.writer.close();
			} else if (stream != null) {
				this.stream.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	public void flushBuffer() throws IOException {
		if (this.stream != null) {
			this.stream.flush();
		}
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (this.writer != null) {
			throw new IllegalStateException(
					"getWriter() has already been called");
		}
		if (this.stream == null) {
			this.stream = this.createOutputStream();
		}
		return this.stream;
	}

	public PrintWriter getWriter() throws IOException {
		if (this.writer != null) {
			return this.writer;
		}
		if (this.stream != null) {
			throw new IllegalStateException(
					"getOutputStream() has already been called");
		}

		this.stream = this.createOutputStream();
		this.writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
		return this.writer;
	}

	// public void reset() {
	// super.reset();
	// this.stream.reset();
	// }
	//
	// public void resetBuffer() {
	// super.resetBuffer();
	// this.stream.reset();
	// }

	public void setContentLength(int length) {

	}
}
