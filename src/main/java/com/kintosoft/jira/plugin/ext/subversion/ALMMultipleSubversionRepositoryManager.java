package com.kintosoft.jira.plugin.ext.subversion;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.kintosoft.svnwebclient.indexing.RevisionIndexer;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import com.opensymphony.module.propertyset.PropertySet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Main component of the Subversion plugin.
 */

@Transactional
public interface ALMMultipleSubversionRepositoryManager {
	String SVN_ROOT_KEY = "svn.root";
	String SVN_REPOSITORY_NAME = "svn.display.name";
	String SVN_USERNAME_KEY = "svn.username";
	String SVN_PASSWORD_KEY = "svn.password";
	String SVN_PRIVATE_KEY_FILE = "svn.privatekeyfile";
	String SVN_SSH_PORT_NUMBER = "svnssh.port.number";
	String SVN_REVISION_INDEXING_KEY = "revision.indexing";
	String SVN_REVISION_CACHE_SIZE_KEY = "revision.cache.size";

	String SVN_DB_SERVER_PORT_NUMBER = "svn.db.server.port.number";
	String SVN_DB_SERVER_PASSWORD = "svn.db.server.password";

	String SVN_LINKFORMAT_TYPE = "linkformat.type";
	String SVN_LINKFORMAT_CHANGESET = "linkformat.changeset";
	String SVN_LINKFORMAT_FILE_ADDED = "linkformat.file.added";
	String SVN_LINKFORMAT_FILE_MODIFIED = "linkformat.file.modified";
	String SVN_LINKFORMAT_FILE_REPLACED = "linkformat.file.replaced";
	String SVN_LINKFORMAT_FILE_DELETED = "linkformat.file.deleted";

	String SVN_LINKFORMAT_PATH_KEY = "linkformat.copyfrom";

	String SVN_LOG_MESSAGE_CACHE_SIZE_KEY = "logmessage.cache.size";

	/**
	 * Returns a Collection of SubversionManager instances, one for each
	 * repository.
	 * 
	 * @return the imanagers.
	 */


	Collection<SubversionManager> getRepositoryList();

	SubversionManager getRepository(long repoId);

	SubversionManager createRepository(SvnProperties props);

	SubversionManager createRepository(long repoId, PropertySet props);

	SubversionManager updateRepository(long repoId, SvnProperties props);

	SubversionManager updateRepository(long repoId, PropertySet props);

	void removeRepository(long repoId);

	void updateIndex() throws Exception;

	List<SVNLogEntry> getLogEntriesByRepository(Issue issue,
												int startIndex, int pageSize, boolean ascending) throws Exception;

	List<SVNLogEntry> getLogEntriesByProject(String projectKey,
											 ApplicationUser user, int startIndex, int pageSize)
			throws Exception;

	List<SVNLogEntry> getLogEntriesByVersion(Version version,
											 ApplicationUser user, int startIndex, int pageSize)
			throws Exception;

	RevisionIndexer getIndexer();

}
