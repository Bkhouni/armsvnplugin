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
package org.polarion.svnwebclient.web.support;

import org.polarion.svnwebclient.SVNWebClientException;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class RequestException extends SVNWebClientException {
	private static final long serialVersionUID = 8944290502416614600L;

	public RequestException() {
		super();
	}

	public RequestException(String message) {
		super(message);
	}

	public RequestException(Throwable cause) {
		super(cause);
	}

	public RequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
