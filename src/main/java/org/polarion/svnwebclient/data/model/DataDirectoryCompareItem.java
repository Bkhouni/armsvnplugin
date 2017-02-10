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
package org.polarion.svnwebclient.data.model;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DataDirectoryCompareItem {
	public static final int OPERATION_ADD = 0;
	public static final int OPERATION_CHANGE = 1;
	public static final int OPERATION_DELETE = 2;

	protected String path;
	protected int operation;
	protected boolean directory;
	protected long oldRevision;
	protected long newRevision;

	public DataDirectoryCompareItem(String path, int operation) {
		this.path = path;
		this.operation = operation;
	}

	public String getPath() {
		return this.path;
	}

	public int getOperation() {
		return this.operation;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public boolean isDirectory() {
		return this.directory;
	}

	public void setOldRevision(long oldRevision) {
		this.oldRevision = oldRevision;
	}

	public long getOldRevision() {
		return this.oldRevision;
	}

	public void setNewRevision(long newRevision) {
		this.newRevision = newRevision;
	}

	public long getNewRevision() {
		return this.newRevision;
	}
}
