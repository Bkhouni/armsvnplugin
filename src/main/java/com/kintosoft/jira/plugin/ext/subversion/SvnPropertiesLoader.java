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

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.InfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * exists to load SubversionManagers the old way so that the
 * MultipleSubversionRepositoryManagerImpl doesn't get krufted up with a bunch
 * of legacy code
 */
public class SvnPropertiesLoader {

	private final static Logger log = LoggerFactory
			.getLogger(SvnPropertiesLoader.class);

	public static final String PROPERTIES_FILE_NAME = "subversion-jira-plugin.properties";

	public static List<SubversionProperties> getSVNProperties()
			throws InfrastructureException {
		Properties allProps = System.getProperties();

		try {
			allProps.load(ClassLoaderUtils.getResourceAsStream(
					PROPERTIES_FILE_NAME,
					ALMMultipleSubversionRepositoryManagerImpl.class));
		} catch (IOException e) {
			throw new InfrastructureException("Problem loading "
					+ PROPERTIES_FILE_NAME + ".", e);
		}

		List propertyList = new ArrayList();
		SubversionProperties defaultProps = getSubversionProperty(-1, allProps);
		if (defaultProps != null) {
			propertyList.add(defaultProps);
		} else {
			log.error("Could not load properties from " + PROPERTIES_FILE_NAME);
			throw new InfrastructureException("Could not load properties from "
					+ PROPERTIES_FILE_NAME);
		}
		SubversionProperties prop;
		int i = 1;
		do {
			prop = getSubversionProperty(i, allProps);
			i++;
			if (prop != null) {
				prop.fillPropertiesFromOther(defaultProps);
				propertyList.add(prop);
			}
		} while (prop != null);

		return propertyList;
	}

	protected static SubversionProperties getSubversionProperty(int index,
			Properties props) {
		String indexStr = "." + Integer.toString(index);
		if (index == -1) {
			indexStr = "";
		}

		if (props
				.containsKey(ALMMultipleSubversionRepositoryManager.SVN_ROOT_KEY
						+ indexStr)) {
			final String svnRootStr = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_ROOT_KEY
							+ indexStr);
			final String displayName = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_REPOSITORY_NAME
							+ indexStr);

			final String changesetFormat = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_CHANGESET
							+ indexStr);
			final String fileAddedFormat = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_ADDED
							+ indexStr);
			final String fileModifiedFormat = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_MODIFIED
							+ indexStr);
			final String fileReplacedFormat = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_REPLACED
							+ indexStr);
			final String fileDeletedFormat = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_DELETED
							+ indexStr);

			final String username = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_USERNAME_KEY
							+ indexStr);
			final String password = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_PASSWORD_KEY
							+ indexStr);
			final String privateKeyFile = props
					.getProperty(ALMMultipleSubversionRepositoryManager.SVN_PRIVATE_KEY_FILE
							+ indexStr);
			Boolean revisionIndexing = null;
			if (props
					.containsKey(ALMMultipleSubversionRepositoryManager.SVN_REVISION_INDEXING_KEY
							+ indexStr)) {
				revisionIndexing = Boolean
						.valueOf("true".equalsIgnoreCase(props
								.getProperty(ALMMultipleSubversionRepositoryManager.SVN_REVISION_INDEXING_KEY
										+ indexStr)));
			}
			Integer revisionCacheSize = null;
			if (props
					.containsKey(ALMMultipleSubversionRepositoryManager.SVN_REVISION_CACHE_SIZE_KEY
							+ indexStr)) {
				revisionCacheSize = new Integer(
						props.getProperty(ALMMultipleSubversionRepositoryManager.SVN_REVISION_CACHE_SIZE_KEY
								+ indexStr));
			}

			return new SubversionProperties().setRoot(svnRootStr)
					.setDisplayName(displayName)
					.setChangeSetFormat(changesetFormat)
					.setFileAddedFormat(fileAddedFormat)
					.setFileModifiedFormat(fileModifiedFormat)
					.setFileReplacedFormat(fileReplacedFormat)
					.setFileDeletedFormat(fileDeletedFormat)
					.setUsername(username).setPassword(password)
					.setPrivateKeyFile(privateKeyFile)
					.setRevisionIndexing(revisionIndexing)
					.setRevisioningCacheSize(revisionCacheSize);

		} else {
			log.info("No "
					+ ALMMultipleSubversionRepositoryManager.SVN_ROOT_KEY
					+ indexStr + " specified in " + PROPERTIES_FILE_NAME);
			return null;
		}
	}
}
