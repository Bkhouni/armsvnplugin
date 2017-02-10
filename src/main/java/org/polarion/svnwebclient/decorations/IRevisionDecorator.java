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
 * 
 */
package org.polarion.svnwebclient.decorations;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public interface IRevisionDecorator {
	/**
	 * @return Title for this decorator section on revision details screen. The
	 *         title should be plain text, it will be formatted according to the
	 *         UI guidelines automatically.
	 */
	String getSectionTitle();

	/**
	 * @param revision
	 * @param request
	 *            The request, which can be used for decorator to cache some
	 *            data in the session.
	 * @return The content of this decorator section on revision details screen.
	 *         It will be rendered under the title. It should be well formed
	 *         HTML and suitable for inserting into table cell. Can be just
	 *         plain text, if no formating is necessary. <br>
	 *         Sample:
	 * 
	 *         <pre>
	 *             My revision decoration, with &lt;b&gt;BOLD&lt;/b&gt; text inside.
	 * </pre>
	 */
	String getSectionContent(String revision, HttpServletRequest request);

	/**
	 * @param revision
	 *            - revision of resource
	 * @param request
	 *            The request, which can be used for decorator to cache some
	 *            data in the session.
	 * @return true - if decorations are present for this revision; false -
	 *         otherwise
	 */
	boolean isRevisionDecorated(String revision, HttpServletRequest request);

	/**
	 * Returns the information for decoration which will be rendered next to the
	 * revision number on file/revision listings.
	 * 
	 * @param revision
	 * @param request
	 *            The request, which can be used for decorator to cache some
	 *            data in the session.
	 * @return IIconDecoration implemetation
	 */
	IIconDecoration getIconDecoration(String revision,
			HttpServletRequest request);
}