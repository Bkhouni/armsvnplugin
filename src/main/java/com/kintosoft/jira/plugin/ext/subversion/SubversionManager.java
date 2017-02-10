/*Copyright (c) "Kinto Soft Ltd"

Subversion ALM is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.*/

package com.kintosoft.jira.plugin.ext.subversion;

import com.kintosoft.jira.plugin.ext.subversion.linkrenderer.SubversionLinkRenderer;
import com.opensymphony.module.propertyset.PropertySet;

public interface SubversionManager {

	long getId();

	String getDisplayName();

	String getRoot();

	String getUsername();

	String getPassword();

	boolean isActive();

	String getInactiveMessage();

	void activate();

	boolean isRevisionIndexing();

	int getRevisioningCacheSize();

	String getPrivateKeyFile();

	ViewLinkFormat getViewLinkFormat();

	SubversionLinkRenderer getLinkRenderer();

	void update(SvnProperties properties);

	void update(PropertySet properties);

	PropertySet getProperties();

	void updateIndex(long latestCachedRevision) throws Exception;

	long getLatestRevision();

	void terminate();

	Thread getIndexingThread();

	boolean isBeingIndexed();
}