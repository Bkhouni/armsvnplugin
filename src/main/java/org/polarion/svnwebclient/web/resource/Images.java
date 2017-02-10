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
package org.polarion.svnwebclient.web.resource;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public interface Images {
	String LOCATION = "images/";

	String FILE = Images.LOCATION + "file.gif";
	String DIRECTORY = Images.LOCATION + "directory.gif";

	String UP = Images.LOCATION + "up.gif";
	String BACK = Images.LOCATION + "back.jpg";
	String HEAD = Images.LOCATION + "head.jpg";
	String BROWSE = Images.LOCATION + "browse.gif";

	String STATISTICS = Images.LOCATION + "stats_btn.gif";
	String COMMIT_GRAPH = Images.LOCATION + "graph_btn.gif";
	String REVISION_LIST = Images.LOCATION + "revisions_btn.gif";
	String ADD_DIRECTORY = Images.LOCATION + "add_dir_btn.gif";
	String ADD_FILE = Images.LOCATION + "add_btn.gif";
	String DELETE = Images.LOCATION + "remove_btn.gif";
	String UPDATE = Images.LOCATION + "update_btn.gif";
	String DOWNLOAD = Images.LOCATION + "download_btn.gif";
	String COMPARE = Images.LOCATION + "compareico.gif";
	String ANNOTATE = Images.LOCATION + "blame_btn.gif";

	String ADDED = Images.LOCATION + "added_ico.gif";
	String DELETED = Images.LOCATION + "removed_ico.gif";
	String MODIFIED = Images.LOCATION + "changed_ico.gif";

	String RESOURCE_ADDED = Images.LOCATION + "resource_added.gif";
	String RESOURCE_DELETED = Images.LOCATION + "resource_removed.gif";
	String RESOURCE_MODIFIED = Images.LOCATION + "resource_changed.gif";

	String DIRECTORY_ADDED = Images.LOCATION + "directory_added.gif";
	String DIRECTORY_DELETED = Images.LOCATION + "directory_removed.gif";
	String DIRECTORY_MODIFIED = Images.LOCATION + "directory_changed.gif";

	String FILE_ADDED = Images.LOCATION + "file_added.gif";
	String FILE_DELETED = Images.LOCATION + "file_removed.gif";
	String FILE_MODIFIED = Images.LOCATION + "file_changed.gif";

	String EMPTY = Images.LOCATION + "pixel.gif";

	String ASC = Images.LOCATION + "ascending.gif";
	String DESC = Images.LOCATION + "descending.gif";
	String DOWNLOAD_DIRECTORY = Images.LOCATION + "download_dir_btn.gif";
}
