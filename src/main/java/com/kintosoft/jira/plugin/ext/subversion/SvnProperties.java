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

import com.opensymphony.module.propertyset.PropertySet;

public interface SvnProperties {
	String getRoot();

	String getDisplayName();

	String getUsername();

	String getPassword();

	Boolean getRevisionIndexing();

	Integer getRevisionCacheSize();

	String getPrivateKeyFile();

	String getWebLinkType();

	String getChangesetFormat();

	String getFileAddedFormat();

	String getViewFormat();

	String getFileModifiedFormat();

	String getFileReplacedFormat();

	String getFileDeletedFormat();

	class Util {
		static PropertySet fillPropertySet(SvnProperties properties,
				PropertySet propertySet) {
			propertySet.setString(
					ALMMultipleSubversionRepositoryManager.SVN_ROOT_KEY,
					properties.getRoot());
			propertySet.setString(
					ALMMultipleSubversionRepositoryManager.SVN_REPOSITORY_NAME,
					properties.getDisplayName() != null ? properties
							.getDisplayName() : properties.getRoot());
			propertySet.setString(
					ALMMultipleSubversionRepositoryManager.SVN_USERNAME_KEY,
					properties.getUsername());
			propertySet.setString(
					ALMMultipleSubversionRepositoryManager.SVN_PASSWORD_KEY,
					SubversionManagerImpl.encryptPassword(properties
							.getPassword()));
			propertySet
					.setBoolean(
							ALMMultipleSubversionRepositoryManager.SVN_REVISION_INDEXING_KEY,
							properties.getRevisionIndexing().booleanValue());
			propertySet
					.setInt(ALMMultipleSubversionRepositoryManager.SVN_REVISION_CACHE_SIZE_KEY,
							properties.getRevisionCacheSize().intValue());
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_PRIVATE_KEY_FILE,
							properties.getPrivateKeyFile());
			propertySet.setString(
					ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_TYPE,
					properties.getWebLinkType());
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_PATH_KEY,
							properties.getViewFormat()); /* SVN-190 */
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_CHANGESET,
							properties.getChangesetFormat());
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_ADDED,
							properties.getFileAddedFormat());
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_MODIFIED,
							properties.getFileModifiedFormat());
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_REPLACED,
							properties.getFileReplacedFormat());
			propertySet
					.setString(
							ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_DELETED,
							properties.getFileDeletedFormat());
			return propertySet;
		}
	}
}
