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
package org.polarion.svnwebclient.util;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.web.support.FormParameters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class Uploader {

	protected Map parameters = new HashMap();
	protected boolean isUploaded;

	public void doPost(HttpServletRequest request,
			HttpServletResponse responce, String destinationDirectory,
			String tempDirectory) throws SVNWebClientException {

		File tempDir = new File(tempDirectory);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}

		File destDir = new File(destinationDirectory);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		DiskFileUpload fu = new DiskFileUpload();
		fu.setHeaderEncoding("UTF-8");
		// maximum size before a FileUploadException will be thrown
		fu.setSizeMax(-1);
		// maximum size that will be stored in memory
		fu.setSizeThreshold(4096);
		// the location for saving data that is larger than getSizeThreshold()
		fu.setRepositoryPath(tempDirectory);
		List fileItems = null;
		try {
			fileItems = fu.parseRequest(request);
		} catch (FileUploadException e) {
			throw new SVNWebClientException("Unable to parse file", e);
		}

		for (Iterator i = fileItems.iterator(); i.hasNext();) {
			FileItem fi = (FileItem) i.next();
			if (fi.isFormField()) {
				this.parameters.put(fi.getFieldName(), fi.getString());
			} else {
				// filename on the client
				String fileName = fi.getName();

				try {
					if (fi.getSize() != 0) {
						this.isUploaded = true;

						// write the file
						File uploadFile = new File(destinationDirectory + "/"
								+ FileUtil.getLastPathElement(fileName));
						fi.write(uploadFile);
					} else {
						this.isUploaded = false;
					}
				} catch (RuntimeException re) {
					throw re;
				} catch (Exception e) {
					throw new SVNWebClientException(e);
				}
				this.parameters.put(FormParameters.FILE_NAME,
						FileUtil.getLastPathElement(fileName));
			}
		}
	}

	public boolean isUploaded() {
		return isUploaded;
	}

	public Map getParameters() {
		return parameters;
	}
}
