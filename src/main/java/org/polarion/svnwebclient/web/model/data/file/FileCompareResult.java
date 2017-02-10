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

import org.polarion.svnwebclient.highlight.HighLighter;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.support.DifferenceLine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileCompareResult {

	protected List diffLines;
	protected String extension;

	public FileCompareResult(List diffLines, String extension) {
		this.diffLines = diffLines;
		this.extension = extension;
	}

	public class Line {
		protected DifferenceLine data;

		public Line(DifferenceLine data) {
			this.data = data;
		}

		public int getChangeType() {
			return this.data.getType();
		}

		public String getImage() {
			if (this.data.getType() == DifferenceLine.ADDED) {
				return Images.ADDED;
			} else if (this.data.getType() == DifferenceLine.DELETED) {
				return Images.DELETED;
			} else if (this.data.getType() == DifferenceLine.MODIFIED) {
				return Images.MODIFIED;
			} else {
				return Images.EMPTY;
			}
		}

		public String getNumber() {
			if (this.data.getNumber() == DifferenceLine.EMPTY_NUMBER) {
				return "&nbsp;";
			} else {
				return Integer.toString(this.data.getNumber() + 1);
			}
		}

		public String getLine() throws Exception {
			String line = this.data.getLine();
			HighLighter highlighter = HighLighter.getHighLighter();
			String fakePath = "foo." + extension;
			String res = highlighter.highlight(line, fakePath);
			return res;
		}

		public String getBackground() {
			if (this.data.getType() == DifferenceLine.ADDED) {
				return "#E0FFE0";
			} else if (this.data.getType() == DifferenceLine.DELETED) {
				return "#FFE3E3";
			} else if (this.data.getType() == DifferenceLine.MODIFIED) {
				return "#FEFFB2";
			} else {
				return "#FFFFFF";
			}
		}
	}

	public List getLines() {
		List ret = new ArrayList();
		for (Iterator i = this.diffLines.iterator(); i.hasNext();) {
			ret.add(new Line((DifferenceLine) i.next()));
		}
		return ret;
	}
}
