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

package org.polarion.svnwebclient.util;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software</A>
 */
public class Encoder {

	public static final String CHARSET_UTF8 = "UTF-8";

	private Encoder() {
		super();
	}

	/**
	 * Encodes the string as 7-bit ASCII string. Non-printing and non 7-bit
	 * characters are encoded in hexadecimal using '%' as encoding flag.
	 * 
	 * @param s
	 *            string to encode
	 */
	public static String encodeUTF8ASCII(String s) {
		try {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				if (ch > 31 && ch < 128) {
					buffer.append(ch);
				} else {
					byte[] bytes = new String(new char[] { ch })
							.getBytes(CHARSET_UTF8);
					for (int j = 0; j < bytes.length; j++) {
						buffer.append('%' + Integer
								.toHexString(bytes[j] & 0xff));
					}
				}
			}
			return buffer.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
