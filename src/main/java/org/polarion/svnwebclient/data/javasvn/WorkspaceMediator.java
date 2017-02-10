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
package org.polarion.svnwebclient.data.javasvn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.io.ISVNWorkspaceMediator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class WorkspaceMediator implements ISVNWorkspaceMediator {
	private Map myTmpFiles = new HashMap();

	public SVNPropertyValue getWorkspaceProperty(String path, String name)
			throws SVNException {
		return null;
	}

	public void setWorkspaceProperty(String path, String name,
			SVNPropertyValue value) throws SVNException {
	}

	public OutputStream createTemporaryLocation(String path, Object id)
			throws SVNException {
		ByteArrayOutputStream tempStorageOS = new ByteArrayOutputStream();
		myTmpFiles.put(id, tempStorageOS);
		return tempStorageOS;
	}

	public InputStream getTemporaryLocation(Object id) throws SVNException {
		return new ByteArrayInputStream(
				((ByteArrayOutputStream) myTmpFiles.get(id)).toByteArray());
	}

	public long getLength(Object id) throws SVNException {
		ByteArrayOutputStream tempStorageOS = (ByteArrayOutputStream) myTmpFiles
				.get(id);
		if (tempStorageOS != null) {
			return tempStorageOS.size();
		}
		return 0;
	}

	public void deleteTemporaryLocation(Object id) {
		myTmpFiles.remove(id);
	}
}
