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
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.file.FileCompareResult;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileCompareDataBean extends AbstractBean {
	protected boolean binary = false;
	protected FileCompareResult fileCompareResult;

	public FileCompareDataBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		List data = null;
		if (this.requestHandler.getStartRevision() != -1) {
			data = (List) this.state.getRequest().getSession()
					.getAttribute(FileCompareBean.START_REVISION_CONTENT);
			this.state.getRequest().getSession()
					.removeAttribute(FileCompareBean.START_REVISION_CONTENT);
			if (this.state.getRequest().getSession()
					.getAttribute(FileCompareBean.START_REVISION_BINARY) != null) {
				this.binary = true;
				this.state.getRequest().getSession()
						.removeAttribute(FileCompareBean.START_REVISION_BINARY);
			}
		}
		if (this.requestHandler.getEndRevision() != -1) {
			data = (List) this.state.getRequest().getSession()
					.getAttribute(FileCompareBean.END_REVISION_CONTENT);
			this.state.getRequest().getSession()
					.removeAttribute(FileCompareBean.END_REVISION_CONTENT);
			if (this.state.getRequest().getSession()
					.getAttribute(FileCompareBean.END_REVISION_BINARY) != null) {
				this.binary = true;
				this.state.getRequest().getSession()
						.removeAttribute(FileCompareBean.END_REVISION_BINARY);
			}
		}

		if (data != null) {
			this.fileCompareResult = new FileCompareResult(data,
					this.getExtension());
		}

		return true;
	}

	public boolean isBinary() {
		return this.binary;
	}

	public FileCompareResult getResult() {
		return this.fileCompareResult;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	protected String getExtension() {
		return this.state.getRequest().getParameter(
				FileCompareBean.PARAM_EXTENSION);
	}

	public String getLangExtension() {
		return FileUtil.getLangExtension(getExtension());
	}
}
