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
package org.polarion.svnwebclient.authorization.impl;

import javax.servlet.*;
import java.io.IOException;

/**
 * This is the placeholder for an implementation.
 */
public class BindSessionsFilter implements Filter {

	/* @see javax.servlet.Filter#init(javax.servlet.FilterConfig) */
	public void init(FilterConfig config) throws ServletException {
		// ignore
	}

	/*
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		// just call the next
		chain.doFilter(request, response);
	}

	/* @see javax.servlet.Filter#destroy() */
	public void destroy() {
		// ignore
	}

}

/*
 * $Log: BindSessionsFilter.java,v $ Revision 1.2 2004/10/26 14:44:13 dobisekm
 * RefProxy dependency removed
 * 
 * Revision 1.1 2004/09/22 13:18:44 dobisekm adding
 */