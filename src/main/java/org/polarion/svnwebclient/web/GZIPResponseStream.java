package org.polarion.svnwebclient.web;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZIPResponseStream extends ServletOutputStream {

	protected ByteArrayOutputStream baos = null;
	protected GZIPOutputStream gzipstream = null;
	protected boolean closed = false;
	protected HttpServletResponse response = null;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		this.response = response;
		this.baos = new ByteArrayOutputStream();
		this.gzipstream = new GZIPOutputStream(this.baos);
	}

	public void close() throws IOException {
		if (this.closed) {
			throw new IOException("This output stream has already been closed");
		}
		this.gzipstream.finish();
		byte[] bytes = this.baos.toByteArray();
		this.response.addHeader("Content-Length",
				Integer.toString(bytes.length));
		this.response.addHeader("Content-Encoding", "gzip");

		// write data to servlet output
		ServletOutputStream output = this.response.getOutputStream();
		output.write(bytes);
		output.flush();
		output.close();

		this.closed = true;
	}

	public void flush() throws IOException {
		if (this.closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		this.gzipstream.flush();
	}

	public void write(int b) throws IOException {
		if (this.closed) {
			throw new IOException("Cannot write to a closed output stream");
		}

		this.gzipstream.write((byte) b);
	}

	public void write(byte b[]) throws IOException {
		if (this.closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		this.write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (this.closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		this.gzipstream.write(b, off, len);
	}

	// public boolean closed() {
	// return this.closed;
	// }
	//
	// public void reset() {
	// try {
	// this.baos = new ByteArrayOutputStream();
	// this.gzipstream = new GZIPOutputStream(this.baos);
	// } catch (IOException ie) {
	// //ignore
	// }
	// }
}
