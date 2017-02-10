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

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class Age {
	protected static final String YEAR = "year";
	protected static final String YEARS = "years";
	protected static final String MONTH = "month";
	protected static final String MONTHS = "months";
	protected static final String WEEK = "week";
	protected static final String WEEKS = "weeks";
	protected static final String DAY = "day";
	protected static final String DAYS = "days";
	protected static final String HOUR = "hour";
	protected static final String HOURS = "hours";
	protected static final String MINUTE = "minute";
	protected static final String MINUTES = "minutes";

	protected String firstDimension = "";
	protected String secondDimension = "";

	public Age(int years, int months, int weeks, int days, int hours,
			int minutes) {
		if (years != 0) {
			this.firstDimension = Integer.toString(years);
			if (years == 1) {
				this.firstDimension += " " + Age.YEAR;
			} else {
				this.firstDimension += " " + Age.YEARS;
			}
			if (months != 0) {
				this.secondDimension = Integer.toString(months);
				if (months == 1) {
					this.secondDimension += " " + Age.MONTH;
				} else {
					this.secondDimension += " " + Age.MONTHS;
				}
			}
		} else if (months != 0) {
			this.firstDimension = Integer.toString(months);
			if (months == 1) {
				this.firstDimension += " " + Age.MONTH;
			} else {
				this.firstDimension += " " + Age.MONTHS;
			}
			if (weeks != 0) {
				this.secondDimension = Integer.toString(weeks);
				if (weeks == 1) {
					this.secondDimension += " " + Age.WEEK;
				} else {
					this.secondDimension += " " + Age.WEEKS;
				}
			}
		} else if (weeks != 0) {
			this.firstDimension = Integer.toString(weeks);
			if (weeks == 1) {
				this.firstDimension += " " + Age.WEEK;
			} else {
				this.firstDimension += " " + Age.WEEKS;
			}
			if (days != 0) {
				this.secondDimension = Integer.toString(days);
				if (days == 1) {
					this.secondDimension += " " + Age.DAY;
				} else {
					this.secondDimension += " " + Age.DAYS;
				}
			}
		} else if (days != 0) {
			this.firstDimension = Integer.toString(days);
			if (days == 1) {
				this.firstDimension += " " + Age.DAY;
			} else {
				this.firstDimension += " " + Age.DAYS;
			}
			if (hours != 0) {
				this.secondDimension = Integer.toString(hours);
				if (hours == 1) {
					this.secondDimension += " " + Age.HOUR;
				} else {
					this.secondDimension += " " + Age.HOURS;
				}
			}
		} else if (hours != 0) {
			this.firstDimension = Integer.toString(hours);
			if (hours == 1) {
				this.firstDimension += " " + Age.HOUR;
			} else {
				this.firstDimension += " " + Age.HOURS;
			}
			if (minutes != 0) {
				this.secondDimension = Integer.toString(minutes);
				if (minutes == 1) {
					this.secondDimension += " " + Age.MINUTE;
				} else {
					this.secondDimension += " " + Age.MINUTES;
				}
			}
		} else if (minutes != 0) {
			this.firstDimension = Integer.toString(minutes);
			if (minutes == 1) {
				this.firstDimension += " " + Age.MINUTE;
			} else {
				this.firstDimension += " " + Age.MINUTES;
			}
		} else {
			this.firstDimension = "0 minutes";
		}
	}

	public String getFirstDimension() {
		return this.firstDimension;
	}

	public String getSecondDimension() {
		return this.secondDimension;
	}

	public String toString() {
		return this.firstDimension
				+ (this.secondDimension.length() > 0 ? " "
						+ this.secondDimension : "");
	}
}
