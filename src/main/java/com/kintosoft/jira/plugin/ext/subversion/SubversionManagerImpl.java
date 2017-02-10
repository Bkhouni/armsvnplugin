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

import com.atlassian.activeobjects.external.ActiveObjects;
import com.kintosoft.jira.plugin.ext.subversion.linkrenderer.LinkFormatRenderer;
import com.kintosoft.jira.plugin.ext.subversion.linkrenderer.NullLinkRenderer;
import com.kintosoft.jira.plugin.ext.subversion.linkrenderer.SubversionLinkRenderer;
import com.kintosoft.svnwebclient.indexing.RevisionIndexer;
import com.kintosoft.svnwebclient.indexing.SVNLogEntryHandlerImpl;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.codec.binary.Base64;
import org.polarion.svncommons.commentscache.authentication.SVNAuthenticationManagerFactory;
import org.polarion.svncommons.commentscache.configuration.ProtocolsConfiguration;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.IOException;
import java.sql.SQLException;

public class SubversionManagerImpl implements Runnable, SubversionManager,
		Comparable<SubversionManager> {
	private final static Logger log = LoggerFactory
			.getLogger(SubversionManagerImpl.class);

	private SVNLogEntryHandlerImpl handler = null;

	private SubversionLinkRenderer linkRenderer;
	private SVNRepository repository;

	private boolean active;
	private String inactiveMessage;
	private long id;

	private PropertySet properties;

	private ViewLinkFormat viewLinkFormat = null;
	private boolean isViewLinkSet = false;

	final RevisionIndexer indexer;


	private final ActiveObjects ao;

	private long latestCachedRevision;

	private Thread indexingThread;

	public SubversionManagerImpl(ActiveObjects ao, RevisionIndexer indexer, long id,
			PropertySet props) {
		this.id = id;
		this.properties = props;
		this.indexer = indexer;
		this.ao = ao;
		setup();
	}

	public synchronized void update(SvnProperties props) {
		deactivate(null);

		SvnProperties.Util.fillPropertySet(props, properties);
		isViewLinkSet = false; /* If we don't reset this flag, we get SVN-190 */

		setup();
	}

	public synchronized void update(PropertySet props) {
		deactivate(null);
		properties = props;
		isViewLinkSet = false; /* If we don't reset this flag, we get SVN-190 */

		setup();
	}

	protected void setup() {
		// Now setup web link renderer
		linkRenderer = null;

		if (getViewLinkFormat() != null)
			linkRenderer = new LinkFormatRenderer(this);
		else
			linkRenderer = new NullLinkRenderer();

		activate();
	}

	public long getId() {
		return id;
	}

	public PropertySet getProperties() {
		return properties;
	}

	public String getDisplayName() {
		return !properties
				.exists(ALMMultipleSubversionRepositoryManager.SVN_REPOSITORY_NAME) ? getRoot()
				: properties
						.getString(ALMMultipleSubversionRepositoryManager.SVN_REPOSITORY_NAME);
	}

	public String getRoot() {
		return properties
				.getString(ALMMultipleSubversionRepositoryManager.SVN_ROOT_KEY);
	}

	public String getUsername() {
		return properties
				.getString(ALMMultipleSubversionRepositoryManager.SVN_USERNAME_KEY);
	}

	public String getPassword() {

		return properties
				.getString(ALMMultipleSubversionRepositoryManager.SVN_PASSWORD_KEY);

	}

	public int getPortNumber() {
		return properties
				.getInt(ALMMultipleSubversionRepositoryManager.SVN_SSH_PORT_NUMBER);
	}

	public boolean isRevisionIndexing() {
		return properties
				.getBoolean(ALMMultipleSubversionRepositoryManager.SVN_REVISION_INDEXING_KEY);
	}

	public int getRevisioningCacheSize() {
		return properties
				.getInt(ALMMultipleSubversionRepositoryManager.SVN_REVISION_CACHE_SIZE_KEY);
	}

	public String getPrivateKeyFile() {
		return properties
				.getString(ALMMultipleSubversionRepositoryManager.SVN_PRIVATE_KEY_FILE);
	}

	public boolean isActive() {
		return active;
	}

	public String getInactiveMessage() {
		return inactiveMessage;
	}

	public void activate() {
		try {
			log.debug("...activating the repository...");
			final SVNURL url = parseSvnUrl();
			log.debug("url=" + url);
			repository = createRepository(url);
			log.debug("the SVNKit repository object has been created");

			ConfigurationProvider confProvider = SWCUtils.getRepository(id);
			log.debug("The repository (id="
					+ id
					+ ") configuration has been retrieved from the active objects:"
					+ confProvider);

			String username = getUsername();
			log.debug("username=" + username);
			String password = getPassword();
			log.debug("password=" + "***********");
			String protocolKeyFile = getPrivateKeyFile();
			log.debug("protocol key file" + protocolKeyFile);
			String protocolPassPhrase = password; // in the Atlassian's
													// configuration password is
													// re-used.
			log.debug("protocol pass phrase=" + "***********");

			String protocolPortNumber = Integer.toString(getPortNumber());
			log.debug("protocol port number=" + protocolPortNumber);

			ProtocolsConfiguration protocols = SWCUtils
					.buildProtocolFromConfiguration(confProvider);

			final ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(username, password, protocols);

			repository.setAuthenticationManager(authManager);
			log.debug("testing the repository connection...");
			repository.testConnection();
			log.debug("... connection succesfully tested: active = true");
			active = true;
		} catch (Exception e) {
			log.error("Connection to Subversion repository " + getRoot()
					+ " failed: " + e.getMessage());
			// We don't want to throw an exception here because then the system
			// won't start if the repo is down
			// or there is something wrong with the configuration. We also still
			// want this repository to show up
			// in our configuration so the user has a chance to fix the problem.
			active = false;
			inactiveMessage = e.getMessage();
		}
	}

	SVNURL parseSvnUrl() throws SVNException {
		return SVNURL.parseURIEncoded(getRoot());
	}

	SVNRepository createRepository(SVNURL url) throws SVNException {
		return SVNRepositoryFactory.create(url);
	}

	private void deactivate(String message) {
		if (repository != null) {
			// try {
			repository.closeSession();
			// }
			// catch (SVNException e) {
			// ignore, we're throwing the repository away anyways
			// }
			repository = null;
		}
		active = false;
		inactiveMessage = message;
	}

	public ViewLinkFormat getViewLinkFormat() {
		if (!isViewLinkSet) {
			final String type = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_TYPE);
			final String linkPathFormat = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_PATH_KEY);
			final String changesetFormat = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_CHANGESET);
			final String fileAddedFormat = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_ADDED);
			final String fileModifiedFormat = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_MODIFIED);
			final String fileReplacedFormat = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_REPLACED);
			final String fileDeletedFormat = properties
					.getString(ALMMultipleSubversionRepositoryManager.SVN_LINKFORMAT_FILE_DELETED);

			if (linkPathFormat != null || changesetFormat != null
					|| fileAddedFormat != null || fileModifiedFormat != null
					|| fileReplacedFormat != null || fileDeletedFormat != null)
				viewLinkFormat = new ViewLinkFormat(type, changesetFormat,
						fileAddedFormat, fileModifiedFormat,
						fileReplacedFormat, fileDeletedFormat, linkPathFormat);
			else
				viewLinkFormat = null; /*
										 * [SVN-190] This could happen if the
										 * user clears all the fields in the
										 * Subversion repository web link
										 * configuration
										 */
			isViewLinkSet = true;
		}

		return viewLinkFormat;
	}

	public SubversionLinkRenderer getLinkRenderer() {
		return linkRenderer;
	}

	public static String decryptPassword(String encrypted) throws IOException {
		if (encrypted == null)
			return null;

		byte[] result = Base64.decodeBase64(encrypted);

		return new String(result, 0, result.length);
	}

	public static String encryptPassword(String password) {
		if (password == null)
			return null;

		return Base64.encodeBase64String(password.getBytes());
	}

	public void updateIndex(long latestCachedRevision) {
		this.latestCachedRevision = latestCachedRevision;
		if (isBeingIndexed()) {
			log.error("There is already an active thread for the repository "
					+ getId());
		}
		indexingThread = new Thread(this);
		indexingThread.setName("Subversion ALM Index: " + id);
		indexingThread.start();
	}

	@Override
	public void run() {
		try {

			if (latestCachedRevision == repository.getLatestRevision()) {
				return;
			}

			SVNRevision start = SVNRevision.create(latestCachedRevision + 1);

			SVNLogClient logClient = new SVNLogClient(
					repository.getAuthenticationManager(), null);

			handler = new SVNLogEntryHandlerImpl(ao,indexer, this,
					repository.getLatestRevision());
			handler.start();
			logClient.doLog(repository.getLocation(), new String[] { "/" },
					SVNRevision.HEAD, start, SVNRevision.HEAD, false, true,
					false, 0, new String[] {}, handler);

		} catch (Exception ex) {
			try {
				handler.stop();
			} catch (SQLException e) {

			}
			active = false;
			inactiveMessage = ex.getMessage();
			log.info(inactiveMessage);
		}
	}

	@Override
	public long getLatestRevision() {
		try {
			return repository.getLatestRevision();
		} catch (SVNException e) {
			active = false;
			inactiveMessage = e.getLocalizedMessage();
			log.info(e.getMessage());
			return -1;
		}
	}

	@Override
	public int compareTo(SubversionManager target) {
		return this.getDisplayName().compareTo(target.getDisplayName());
	}

	@Override
	synchronized public void terminate() {
		handler.terminate();
	}

	@Override
	public Thread getIndexingThread() {
		return indexingThread;
	}

	@Override
	public boolean isBeingIndexed() {
		return indexingThread != null && indexingThread.isAlive();
	}
}
