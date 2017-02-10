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
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.util.contentencoding.ContentEncodingHelper;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.file.FileAnnotation;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileAnnotationBean extends AbstractBean {
	protected List annotation;
	protected boolean binary;

	public FileAnnotationBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		String fileName = UrlUtil.getLastPathElement(this.requestHandler
				.getUrl());
		String containerMimeType = this.state.getSession().getServletContext()
				.getMimeType(fileName.toLowerCase());

		this.binary = dataProvider.isBinaryFile(this.requestHandler.getUrl(),
				this.requestHandler.getCurrentRevision(), containerMimeType);
		if (!this.binary) {
			String encoding = ContentEncodingHelper.getEncoding(
					dataProvider.getId(), this.state);
			this.annotation = dataProvider.getAnnotation(
					this.requestHandler.getUrl(),
					this.requestHandler.getCurrentRevision(), encoding);
		}
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public boolean isBinaryFile() {
		return this.binary;
	}

	public FileAnnotation getFileAnnotation() {
		return new FileAnnotation(this.annotation, this.requestHandler.getUrl());
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}

	public String getLangExtension() {
		String ext = FileUtil.getExtension(this.requestHandler.getUrl());
		return FileUtil.getLangExtension(ext);
	}
}
