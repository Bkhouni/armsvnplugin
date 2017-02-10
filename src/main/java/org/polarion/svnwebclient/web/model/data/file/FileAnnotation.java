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
package org.polarion.svnwebclient.web.model.data.file;

import org.polarion.svnwebclient.data.model.DataAnnotationElement;
import org.polarion.svnwebclient.highlight.HighLighter;
import org.polarion.svnwebclient.util.DateFormatter;
import org.polarion.svnwebclient.util.HtmlUtil;
import org.polarion.svnwebclient.util.NumberFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileAnnotation {

	protected String url;
	protected List annotationElements;

	public FileAnnotation(List annotationElements, String url) {
		this.annotationElements = annotationElements;
		this.url = url;
	}

	public class AnnotatedLine {
		protected DataAnnotationElement annotationElement;

		public AnnotatedLine(DataAnnotationElement annotationElement) {
			this.annotationElement = annotationElement;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.annotationElement.getRevision());
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.annotationElement
					.getRevision()));
		}

		public String getDate() {
			return DateFormatter.format(this.annotationElement.getDate());
		}

		public String getAge() {
			return DateFormatter.format(this.annotationElement.getDate(),
					DateFormatter.RELATIVE);
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.annotationElement.getAuthor());
		}

		public String getLine() throws Exception {
			String line = this.annotationElement.getLine();

			HighLighter highlighter = HighLighter.getHighLighter();
			String res = highlighter.highlight(line, url);
			return res;
		}
	}

	public List getAnnotatedLines() {
		List ret = new ArrayList();
		for (Iterator i = this.annotationElements.iterator(); i.hasNext();) {
			ret.add(new AnnotatedLine((DataAnnotationElement) i.next()));
		}
		return ret;
	}
}
