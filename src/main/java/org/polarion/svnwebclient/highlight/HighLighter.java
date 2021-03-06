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
package org.polarion.svnwebclient.highlight;

import com.uwyn.jhighlight.renderer.Renderer;
import org.polarion.svnwebclient.util.FileUtil;
import org.polarion.svnwebclient.util.HtmlUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HighLighter {

	protected final Map rendererMappings = new TreeMap();

	protected String encoding = "UTF-8";

	protected final static HighLighter highlighter = new HighLighter();

	protected HighLighter() {
		/*
		 * this.rendererMappings.put("java", new JavaXhtmlRenderer());
		 * this.rendererMappings.put("html", new XmlXhtmlRenderer());
		 * this.rendererMappings.put("xml", new XmlXhtmlRenderer());
		 * this.rendererMappings.put("jsp", new XmlXhtmlRenderer());
		 * this.rendererMappings.put("c", new CppXhtmlRenderer());
		 * this.rendererMappings.put("cpp", new CppXhtmlRenderer());
		 * this.rendererMappings.put("h", new CppXhtmlRenderer());
		 * this.rendererMappings.put("hpp", new CppXhtmlRenderer());
		 */
	}

	public static HighLighter getHighLighter() {
		return highlighter;
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	protected Renderer getRenderer(String fileExtension) {
		return (Renderer) this.rendererMappings
				.get(fileExtension.toLowerCase());
	}

	public String highlight(String data, Renderer renderer) throws IOException {
		String res = null;

		if (data != null) {
			if (renderer == null) {
				res = HtmlUtil.encodeWithSpace(data);
			} else {
				res = highlight(renderer, null, data, this.encoding, true);
				// remove starting: <!-- : generated by JHighlight v1.0
				// (http://jhighlight.dev.java.net) -->
				res = res.substring(73);
				if ("".equals(res)) {
					res = "&nbsp";
				} else {
					// remove ending: <br />
					res = res.replaceAll("<br />", "").trim();
				}
			}
		} else {
			res = "";
		}

		return res;
	}

	/**
	 * There is a bug in JHighlight in method
	 * XhtmlRenderer.highlight(String,String,String,boolean) - UTF-8 text gets
	 * broken by it. This method is fix of this bug.
	 */
	private static String highlight(Renderer renderer, String name, String in,
			String encoding, boolean fragment) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		renderer.highlight(name,
				new ByteArrayInputStream(in.getBytes(encoding)), out, encoding,
				fragment);
		return out.toString(encoding);
	}

	public String highlight(String content, String url) throws IOException {
		Renderer renderer = null;

		String fileExtension = FileUtil.getExtension(url);
		if (fileExtension != null) {
			renderer = this.getRenderer(fileExtension);
		}

		return this.highlight(content, renderer);
	}

	public String getColorizedContent(byte[] content, String fileExtension)
			throws IOException {
		return this.highlight(new String(content), fileExtension);
	}

}
