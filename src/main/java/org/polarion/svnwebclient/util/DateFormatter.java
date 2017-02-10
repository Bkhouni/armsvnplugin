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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DateFormatter {
	public static final int ABSOLUT = 0;
	public static final int RELATIVE = 1;

	protected static final String ABSOLUT_FORMAT = "yyyy-MM-dd HH:mm";

	public static String format(Date date) {
		return DateFormatter.format(date, DateFormatter.ABSOLUT);
	}

	public static String format(Date date, int type) {
		if (date == null) {
			return "";
		}
		if (type == DateFormatter.ABSOLUT) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					DateFormatter.ABSOLUT_FORMAT);
			return dateFormat.format(date);
		} else if (type == DateFormatter.RELATIVE) {
			return DateFormatter.getAge(date).toString();
		} else {
			return date.toString();
		}
	}

	protected static Age getAge(Date date) {
		ElapsedTime elapsedTime = new ElapsedTime(date, new Date());
		return new Age(elapsedTime.getYears(), elapsedTime.getMonths(),
				elapsedTime.getWeeks(), elapsedTime.getDays(),
				elapsedTime.getHours(), elapsedTime.getMinutes());
	}
}
