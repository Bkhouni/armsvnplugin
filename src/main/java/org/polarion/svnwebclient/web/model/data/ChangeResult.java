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
package org.polarion.svnwebclient.web.model.data;

import org.polarion.svnwebclient.util.CommentUtil;
import org.polarion.svnwebclient.util.DateFormatter;
import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.util.NumberFormatter;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.resource.Images;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class ChangeResult {
	protected String message;
	protected List elements;
	protected Navigation navigation;
	protected boolean successful;

	public static class Element {
		protected String name;
		protected String author;
		protected Date date;
		protected long revision;
		protected String comment;
		protected boolean directory;
		protected long size;

		public void setDirectory(boolean directory) {
			this.directory = directory;
		}

		public String getImage() {
			if (this.directory) {
				return Images.DIRECTORY;
			} else {
				return Images.FILE;
			}
		}

		public boolean isDirectory() {
			return this.directory;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return HtmlUtil.encode(this.name);
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.author);
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getDate() {
			return DateFormatter.format(this.date);
		}

		public String getAge() {
			return DateFormatter.format(this.date, DateFormatter.RELATIVE);
		}

		public void setRevision(long revision) {
			this.revision = revision;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.revision);
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.revision));
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getComment() {
			return HtmlUtil.encode(this.comment);
		}

		public String getFirstLine() {
			return CommentUtil.getFirstLine(this.comment);
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.comment);
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.comment);
		}

		public void setSize(long size) {
			this.size = size;
		}

		public String getSize() {
			String ret;
			if (this.directory) {
				ret = "<DIR>";
			} else {
				ret = NumberFormatter.format(this.size);
			}
			return HtmlUtil.encode(ret);
		}
	}

	public ChangeResult(boolean successful, String message, List elements,
			Navigation navigation) {
		this.successful = successful;
		this.message = message;
		this.elements = elements;
		this.navigation = navigation;
	}

	public boolean isSuccessful() {
		return this.successful;
	}

	public String getMessage() {
		return this.message;
	}

	public List getElements() {
		return this.elements;
	}

	public Navigation getNavigation() {
		return this.navigation;
	}
}
