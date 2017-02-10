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

import org.polarion.svnwebclient.configuration.ConfigurationProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DataRevision {
	public static final int TYPE_ADDED = 0;
	public static final int TYPE_MODIFIED = 1;
	public static final int TYPE_REPLACED = 2;
	public static final int TYPE_DELETED = 3;

	protected long revision;
	protected String author;
	protected Date date;
	protected String comment;
	protected List changedElements = new ArrayList();

	public class ChangedElement {
		protected int type;
		protected String path;
		protected String copyPath;
		protected long copyRevision;

		public ChangedElement(int type, String path, String copyPath,
				long copyRevison) {
			this.type = type;
			this.path = path;
			this.copyPath = copyPath;
			this.copyRevision = copyRevison;
		}

		public String getCopyPath() {
			return this.copyPath;
		}

		public long getCopyRevision() {
			return this.copyRevision;
		}

		public int getType() {
			return this.type;
		}

		public String getPath() {
			return this.path;
		}
	}

	public long getRevision() {
		return this.revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return ConfigurationProvider.getAuthorDecorator().getAuthorName(
				this.author);
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void addChangedElement(int type, String path, String copyPath,
			long copyRevison) {
		this.changedElements.add(new ChangedElement(type, path, copyPath,
				copyRevison));
	}

	public List getChangedElements() {
		return this.changedElements;
	}
}
