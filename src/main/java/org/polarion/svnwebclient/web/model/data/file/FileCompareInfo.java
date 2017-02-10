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

import java.util.ArrayList;
import java.util.List;

public class FileCompareInfo {
	protected List stopPoints = new ArrayList();
	protected int modifiedItemsCount;
	protected int deletedItemsCount;
	protected int addedItemsCount;

	public static class StopPoints {
		protected int lineNumber;
		protected String frameName;
		protected int type;
		protected int blockPosition;

		public StopPoints(int lineNumber, String frameName, int type,
				int blockPosition) {
			this.lineNumber = lineNumber;
			this.frameName = frameName;
			this.type = type;
			this.blockPosition = blockPosition;
		}

		public String getFrameName() {
			return this.frameName;
		}

		public int getLineNumber() {
			return this.lineNumber;
		}

		public int getBlockPosition() {
			return this.blockPosition;
		}

		public void setBlockPosition(int blockPosition) {
			this.blockPosition = blockPosition;
		}

		public int getType() {
			return this.type;
		}
	}

	public List getStopPoints() {
		return this.stopPoints;
	}

	public void setStopPoint(int lineNumber, String frameName, int type,
			int blockPosition) {
		this.stopPoints.add(new StopPoints(lineNumber, frameName, type,
				blockPosition));
	}

	public FileCompareInfo.StopPoints getLastProperElement(int position,
			int type) {
		FileCompareInfo.StopPoints point = null;
		if (this.stopPoints.size() > 0) {
			point = (FileCompareInfo.StopPoints) this.stopPoints
					.get(this.stopPoints.size() - 1);
			if (!(point.getBlockPosition() == position && point.getType() == type)) {
				point = null;
			}
		}
		return point;
	}

	public int getAddedItemsCount() {
		return this.addedItemsCount;
	}

	public void increaseAddedItemsCount() {
		this.addedItemsCount++;
	}

	public int getDeletedItemsCount() {
		return this.deletedItemsCount;
	}

	public void increaseDeletedItemsCount() {
		this.deletedItemsCount++;
	}

	public int getModifiedItemsCount() {
		return this.modifiedItemsCount;
	}

	public void increaseModifiedItemsCount() {
		this.modifiedItemsCount++;
	}

}
