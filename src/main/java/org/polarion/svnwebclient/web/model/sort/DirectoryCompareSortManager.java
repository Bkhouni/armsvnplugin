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
package org.polarion.svnwebclient.web.model.sort;

import org.polarion.svnwebclient.data.model.DataDirectoryCompareItem;

import java.util.Comparator;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryCompareSortManager {
	public Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if (o1 == null || o2 == null) {
					return 0;
				}
				return ((DataDirectoryCompareItem) o1).getPath().compareTo(
						((DataDirectoryCompareItem) o2).getPath());
			}
		};
	}
}
