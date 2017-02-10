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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 * 
 *         Diff format ranges meaning: (e.g. @@ -5,16 +11,10 @@) The chunk range
 *         for the original should be the sum of all contextual and deletion
 *         (including changed) chunk lines. The chunk range for the new file
 *         should be a sum of all contextual and addition (including changed)
 *         chunk lines.
 * 
 *         If a line is modified, it is represented as a deletion and addition.
 */
public class DifferenceModel {

	protected List areas = new ArrayList();

	public DifferenceModel(String difference) {
		Pattern header = Pattern
				.compile("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@");
		String[] lines = difference.split("\\r\\n|\\r|\\n");
		DifferenceArea area = null;
		for (int i = 0; i < lines.length; i++) {

			Matcher matcher = header.matcher(lines[i]);
			if (matcher.matches()) {
				String leftIndex = this.checkGroup(matcher.group(1));
				String leftSize = this.checkGroup(matcher.group(2));
				String rightIndex = this.checkGroup(matcher.group(3));
				String rightSize = this.checkGroup(matcher.group(4));

				area = new DifferenceArea(Integer.parseInt(leftIndex) - 1,
						Integer.parseInt(leftSize),
						Integer.parseInt(rightIndex) - 1,
						Integer.parseInt(rightSize));
				this.areas.add(area);
			} else {
				if (area != null) {
					area.addElement(lines[i]);
				}
			}
		}
	}

	protected String checkGroup(String num) {
		String res = null;
		if (num == null) {
			res = "1";
		} else {
			int index = num.indexOf(",");
			if (index != -1) {
				res = num.substring(1);
			} else {
				res = num;
			}
		}

		return res;
	}

	public List getLeftLines(String left) {
		List ret = new ArrayList();
		String[] lines = left.split("\\r\\n|\\r|\\n");
		int index = 0;

		/**
		 * As contexts from difference areas may overlay, we need to add check
		 * not to add the same lines twice, e.g.:
		 * 
		 * @@ -10,6 +10,7 @@ ILocation loc = (ILocation) iter.next();
		 *    resourceCreated(loc); } + return; } if (!isOurLocation(newLoc)) {
		 *    // only oldLoc is ours
		 * @@ -14,6 +15,7 @@ if (!isOurLocation(newLoc)) { // only oldLoc is
		 *    ours resourceRemoved(oldLoc); + return; } Collection filesMoved =
		 *    getFileSubLocations(newLoc); Iterator iter =
		 *    filesMoved.iterator();
		 * 
		 *    Here following lines are encountered in both contexts: if
		 *    (!isOurLocation(newLoc)) { // only oldLoc is ours
		 */
		Set diffAreaContextLineNumbers = new HashSet();

		for (Iterator i = this.areas.iterator(); i.hasNext();) {
			DifferenceArea area = (DifferenceArea) i.next();

			// add lines and mark them as unchanged, which are encountered
			// before current diff area or
			// between current diff area and previous diff area
			for (int j = index; j < area.getLeftIndex(); j++) {
				ret.add(new DifferenceLine(j, DifferenceLine.NOT_CHANGED,
						lines[j]));
			}

			// process lines from diff area
			List leftElements = area.getLeftElements();
			Iterator iter = leftElements.iterator();
			while (iter.hasNext()) {
				DifferenceLine diffLine = (DifferenceLine) iter.next();
				if (diffLine.getNumber() == DifferenceLine.EMPTY_NUMBER
						|| (diffLine.getNumber() != DifferenceLine.EMPTY_NUMBER && diffAreaContextLineNumbers
								.add(new Integer(diffLine.getNumber())))) {
					ret.add(diffLine);
				}
			}

			index = area.getLeftIndex() + area.getLeftSize();
		}

		// add lines and mark them as unchanged, which are encountered after
		// last diff area
		if (index >= 0) {
			for (int i = index; i < lines.length; i++) {
				ret.add(new DifferenceLine(i, DifferenceLine.NOT_CHANGED,
						lines[i]));
			}
		}
		return ret;
	}

	public List getRightLines(String right) {
		List ret = new ArrayList();
		String[] lines = right.split("\\r\\n|\\r|\\n");
		int index = 0;

		/**
		 * As contexts from difference areas may overlay, we need to add check
		 * not to add the same lines twice
		 */
		Set diffAreaContextLineNumbers = new HashSet();

		for (Iterator i = this.areas.iterator(); i.hasNext();) {
			DifferenceArea area = (DifferenceArea) i.next();

			// add lines and mark them as unchanged, which are encountered
			// before current diff area or
			// between current diff area and previous diff area
			for (int j = index; j < area.getRightIndex(); j++) {
				ret.add(new DifferenceLine(j, DifferenceLine.NOT_CHANGED,
						lines[j]));
			}

			// process lines from diff area
			List rightElements = area.getRightElements();
			Iterator iter = rightElements.iterator();
			while (iter.hasNext()) {
				DifferenceLine diffLine = (DifferenceLine) iter.next();
				if (diffLine.getNumber() == DifferenceLine.EMPTY_NUMBER
						|| (diffLine.getNumber() != DifferenceLine.EMPTY_NUMBER && diffAreaContextLineNumbers
								.add(new Integer(diffLine.getNumber())))) {
					ret.add(diffLine);
				}
			}

			index = area.getRightIndex() + area.getRightSize();
		}

		// add lines and mark them as unchanged, which are encountered after
		// last diff area
		if (index >= 0) {
			for (int i = index; i < lines.length; i++) {
				ret.add(new DifferenceLine(i, DifferenceLine.NOT_CHANGED,
						lines[i]));
			}
		}
		return ret;
	}

	public static List getUntouchedLines(String content) {
		List ret = new ArrayList();
		String[] lines = content.split("\\r\\n|\\r|\\n");
		for (int i = 0; i < lines.length; i++) {
			ret.add(new DifferenceLine(i, DifferenceLine.NOT_CHANGED, lines[i]));
		}
		return ret;
	}
}
