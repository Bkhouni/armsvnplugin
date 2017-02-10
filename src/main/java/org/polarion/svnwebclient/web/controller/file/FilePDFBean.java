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
package org.polarion.svnwebclient.web.controller.file;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.util.FileUtil;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.file.FileData;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FilePDFBean extends AbstractBean {

	protected FileData fileData;

	public FilePDFBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		/*
		 * String fileName = UrlUtil.getLastPathElement(this.requestHandler
		 * .getUrl()); String containerMimeType =
		 * this.state.getSession().getServletContext()
		 * .getMimeType(fileName.toLowerCase());
		 * 
		 * DataFile file =
		 * dataProvider.getFileData(this.requestHandler.getUrl(),
		 * this.requestHandler.getCurrentRevision(), containerMimeType);
		 * 
		 * try { String encoding = ContentEncodingHelper.getEncoding(
		 * dataProvider.getId(), this.state); String content =
		 * ContentEncodingHelper.encodeBytes( file.getContent(), encoding);
		 * 
		 * String fileExtension = FileUtil.getExtension(this.requestHandler
		 * .getUrl()); this.fileData = new FileData(content, fileExtension); }
		 * catch (IOException ie) { throw new SVNWebClientException(ie); }
		 */
		return true;

	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	protected void redirectToDownload() throws SVNWebClientException {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_DOWNLOAD, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.requestHandler.getUrl()));
		urlGenerator.addParameter(RequestParameters.CREV,
				Long.toString(this.requestHandler.getCurrentRevision()));
		try {
			this.state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (IOException e) {
			throw new SVNWebClientException(e);
		}
	}

	public FileData getFileData() {
		return this.fileData;
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}

	public String getLangExtension() {
		String fileExtension = FileUtil.getExtension(this.requestHandler
				.getUrl());
		return FileUtil.getLangExtension(fileExtension);
	}
}
