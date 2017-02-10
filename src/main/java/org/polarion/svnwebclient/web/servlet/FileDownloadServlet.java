/*
 * Copyright (c) 2004, 2005 Polarion Software, All rights reserved.
 * Email: community@polarion.org
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"). You may not use
 * this file except in compliance with the License. Copy of the License is
 * located in the file LICENSE.txt in the project distribution. You may also
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.polarion.svnwebclient.web.servlet;

import org.apache.log4j.Logger;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataFileElement;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileDownloadServlet extends AbstractServlet {
	private static final long serialVersionUID = -5061766135434675105L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider, State state)
			throws SVNWebClientException {
		AbstractRequestHandler requestHandler = this.getRequestHandler(state
				.getRequest());

		OutputStream outStream = null;
		try {
			long revision = dataProvider.getHeadRevision();
			if (requestHandler.getCurrentRevision() != -1) {
				revision = requestHandler.getCurrentRevision();
			}
			String url = requestHandler.getUrl();
			if (requestHandler.getPegRevision() != -1) {
				url = dataProvider.getLocation(requestHandler.getUrl(),
						requestHandler.getPegRevision(), revision);
			}

			String name = UrlUtil.getLastPathElement(url);
			String mimeType = this.getServletContext().getMimeType(
					name.toLowerCase());

			DataFileElement fileElement = dataProvider.getFile(url, revision,
					mimeType);
			String fileName = fileElement.getName();

			if (mimeType == null) {
				mimeType = "application/Octet-stream";
			}
			state.getResponse().setContentType(mimeType);

			String filenameAttr = UrlUtil.getFilenameAttribute(fileName,
					state.getRequest());
			String contentDisposition = null;
			if (requestHandler.isAttachment()) {
				contentDisposition = "attachment";
			} else {
				contentDisposition = "inline";
			}
			state.getResponse().setHeader("Content-Disposition",
					contentDisposition + "; " + filenameAttr);

			outStream = state.getResponse().getOutputStream();
			byte[] content = fileElement.getContent();
			outStream.write(content, 0, content.length);
			outStream.flush();
		} catch (SVNWebClientException ex) {
			try {
				state.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				throw new SVNWebClientException(e);
			}
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (Exception e) {
					Logger.getInstance(FileDownloadServlet.class).error(e, e);
				}
			}
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
