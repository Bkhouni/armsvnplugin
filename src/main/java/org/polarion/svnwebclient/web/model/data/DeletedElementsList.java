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
import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.util.NumberFormatter;

import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DeletedElementsList {
	protected List deletedElements;

	public static class Element {
		protected String type;
		protected String name;
		protected String revision;
		protected String size;
		protected String date;
		protected String age;
		protected String author;
		protected String comment;

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getName() {
			return HtmlUtil.encode(this.name);
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(Long.parseLong(this.revision));
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(this.revision);
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

		public String getSize() {
			return this.size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getDate() {
			return this.date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getAge() {
			return this.age;
		}

		public void setAge(String age) {
			this.age = age;
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.author);
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getFirstLine() {
			return CommentUtil.getFirstLine(this.comment);
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.comment);
		}

		public String getComment() {
			return HtmlUtil.encode(this.comment);
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.comment);
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

	}

	public List getDeletedElements() {
		return this.deletedElements;
	}

	public void setDeletedElements(List elements) {
		this.deletedElements = elements;
	}
}
