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

import org.polarion.svnwebclient.highlight.HighLightLineHandler;

import java.io.IOException;
import java.util.Iterator;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileData {

	protected final String fileContent;
	protected final String fileExtension;

	protected final int linesCount;

	public FileData(String fileContent, String fileExtension) {
		this.fileContent = fileContent;
		this.fileExtension = fileExtension;

		HighLightLineHandler handler = new HighLightLineHandler(fileContent,
				fileExtension);
		this.linesCount = handler.size();
	}

	public static class Line {
		protected long number;
		protected String data;

		public Line(long number, String data) {
			this.number = number;
			this.data = data;
		}

		public String getNumber() {
			return Long.toString(this.number);
		}

		public String getData() {
			return this.data;
		}
	}

	protected class IteratorImpl implements Iterator {

		protected final HighLightLineHandler handler;
		protected int number = 1;

		public IteratorImpl() {
			this.handler = new HighLightLineHandler(fileContent, fileExtension);
		}

		public boolean hasNext() {
			return this.handler.hasLines();
		}

		public Object next() {
			try {
				String strLine = this.handler.getLine();
				return new Line(this.number++, strLine);
			} catch (IOException ie) {
				throw new RuntimeException("Unable to highlight line content",
						ie);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator getLinesIterator() {
		return new IteratorImpl();
	}

	public int getLinesCount() {
		return this.linesCount;
	}
}
