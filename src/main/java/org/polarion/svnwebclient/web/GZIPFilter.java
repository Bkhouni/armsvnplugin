package org.polarion.svnwebclient.web;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Compression filter It compresses content if a client browser supports 'gzip'
 * encoding, otherwise it returns the content as it is
 * 
 */
public class GZIPFilter implements Filter {

	protected Logger logger = Logger.getLogger(GZIPFilter.class);

	protected FilterConfig config;

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;

			String acceptEncoding = request.getHeader("accept-encoding");
			if (acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1) {
				this.logger.debug("GZIP supported, compressing.");
				GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(
						response);
				chain.doFilter(req, wrappedResponse);
				wrappedResponse.finishResponse();
				return;
			} else {
				chain.doFilter(req, res);
			}
		}
	}

	// public void doFilter(ServletRequest req, ServletResponse res, FilterChain
	// chain) throws IOException, ServletException {
	// chain.doFilter(req, res);
	// }

	public void destroy() {
		this.config = null;
	}

}
