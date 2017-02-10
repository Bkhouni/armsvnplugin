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

import org.polarion.svnwebclient.data.model.DataDirectoryCompareItem;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class Editor implements ISVNEditor {
	public List changedItems = new ArrayList();
	public List stack = new ArrayList();

	public List getChangedItems() {
		return this.changedItems;
	}

	public void targetRevision(long revision) throws SVNException {
	}

	public void openRoot(long revision) throws SVNException {
	}

	public void deleteEntry(String path, long revision) throws SVNException {
		this.changedItems.add(new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_DELETE));
	}

	public void absentDir(String path) throws SVNException {
	}

	public void absentFile(String path) throws SVNException {
	}

	public void addDir(String path, String copyFromPath, long copyFromRevision)
			throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_ADD);
		item.setOldRevision(-1);
		item.setDirectory(true);
		this.stack.add(0, item);
	}

	public void openDir(String path, long revision) throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_CHANGE);
		item.setOldRevision(revision);
		item.setDirectory(true);
		this.stack.add(0, item);
	}

	public void changeDirProperty(String name, SVNPropertyValue value)
			throws SVNException {
		if (this.stack.size() > 0) {
			String strValue = SVNPropertyValue.getPropertyAsString(value);
			DataDirectoryCompareItem item = (DataDirectoryCompareItem) this.stack
					.get(0);
			if (SVNProperty.COMMITTED_REVISION.equals(name)) {
				item.setNewRevision(Long.parseLong(strValue));
			}
		}
	}

	public void closeDir() throws SVNException {
		if (this.stack.size() > 0) {
			this.changedItems.add(this.stack.remove(0));
		}
	}

	public void addFile(String path, String copyFromPath, long copyFromRevision)
			throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_ADD);
		item.setOldRevision(-1);
		item.setDirectory(false);
		this.stack.add(0, item);
	}

	public void openFile(String path, long revision) throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_CHANGE);
		item.setOldRevision(revision);
		item.setDirectory(false);
		this.stack.add(0, item);
	}

	public void applyTextDelta(String path, String baseChecksum)
			throws SVNException {
	}

	public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow)
			throws SVNException {
		return null;
	}

	public void textDeltaEnd(String path) throws SVNException {
	}

	public void changeFileProperty(String path, String name,
			SVNPropertyValue value) throws SVNException {
		if (this.stack.size() > 0) {
			String strValue = SVNPropertyValue.getPropertyAsString(value);
			DataDirectoryCompareItem item = (DataDirectoryCompareItem) this.stack
					.get(0);
			if (SVNProperty.COMMITTED_REVISION.equals(name)) {
				item.setNewRevision(Long.parseLong(strValue));
			}
		}
	}

	public void closeFile(String path, String textChecksum) throws SVNException {
		if (this.stack.size() > 0) {
			this.changedItems.add(this.stack.remove(0));
		}
	}

	public SVNCommitInfo closeEdit() throws SVNException {
		return null;
	}

	public void abortEdit() throws SVNException {
	}
}
