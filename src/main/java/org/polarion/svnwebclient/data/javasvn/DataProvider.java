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
package org.polarion.svnwebclient.data.javasvn;

import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.opensymphony.util.TextUtils;
import org.polarion.svncommons.commentscache.CommentsCache;
import org.polarion.svncommons.commentscache.CommentsCacheConfig;
import org.polarion.svncommons.commentscache.CommentsCacheException;
import org.polarion.svncommons.commentscache.authentication.SVNAuthenticationManagerFactory;
import org.polarion.svncommons.commentscache.configuration.ProtocolsConfiguration;
import org.polarion.svnwebclient.authorization.UserCredentials;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.configuration.WebConfigurationProvider;
import org.polarion.svnwebclient.data.AuthenticationException;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.data.model.*;
import org.polarion.svnwebclient.util.FileUtil;
import org.polarion.svnwebclient.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import org.w3c.util.UUID;

import java.io.*;
import java.util.*;

/**
 * Implements data provider interface using JavaSVN library
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DataProvider implements IDataProvider {

	private static Logger log = LoggerFactory.getLogger(DataProvider.class);

	protected static final String MIME_TYPE_BINARY = "application/octet-stream";
	protected String id;
	protected SVNRepository repository;
	protected SVNURL repositoryRoot;
	protected UserCredentials userCredentials;
	protected String reposLocation;

	public static void startup(String userName, String password, String id,
			String url) throws DataProviderException {
		try {
			DAVRepositoryFactory.setup();
			SVNRepositoryFactoryImpl.setup();

			CommentsCacheConfig cacheConfig = new CommentsCacheConfig(url,
					userName, password, ConfigurationProvider.getInstance(id)
							.getCacheDirectory(), ConfigurationProvider
							.getInstance(id).getCachePageSize());

			// One CommentsCache per id (rootUrl) and One SVNRepository
			// (internal) for comments per url with active users count
			CommentsCache.init(cacheConfig, id, url);
			CommentsCache.setForcedHttpAuth(ConfigurationProvider.getInstance(
					id).isForcedHttpAuth());

			// One SVNRepository per id with active users count
			org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool.init(id,
					ConfigurationProvider.getInstance(id)
							.getSvnConnectionsCount(), url, userName, password);
			CommentsCache.getInstance(id, userName, password).prefetch(
					ConfigurationProvider.getInstance(id)
							.getCachePrefetchMessagesCount());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new DataProviderException(e);
		}
	}

	public static String getID(ConfigurationProvider confProvider, String url,
			String name, String password) throws SVNException {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();

		SVNRepository repository = null;
		String res = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL
					.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(name, password, SWCUtils
							.buildProtocolFromConfiguration(confProvider));
			repository.setAuthenticationManager(authManager);
			SVNURL root = repository.getRepositoryRoot(true);
			res = root.toString();
		} finally {
			if (repository != null)
				repository.closeSession();
		}

		return res;
	}

	public static void verify(ConfigurationProvider confProvider, String url,
			String name, String password) throws SVNException {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		SVNRepository repository = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL
					.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(name, password, SWCUtils
							.buildProtocolFromConfiguration(confProvider));
			repository.setAuthenticationManager(authManager);
			repository.testConnection();
		} finally {
			if (repository != null) {
				repository.closeSession();
			}
		}
	}

	public static String getRoot(Properties props) throws SVNException {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();

		SVNRepository repository = null;
		String res = null;
		try {
			log.debug("Creating the Subversion repository for the input url...");
			repository = SVNRepositoryFactory.create(SVNURL
					.parseURIEncoded(props
							.getProperty(WebConfigurationProvider.ROOT_URL)));
			log.debug("... the repository has been created");
			String username = props
					.getProperty(WebConfigurationProvider.USERNAME);
			log.debug("username: " + username);
			String password = props
					.getProperty(WebConfigurationProvider.PASSWORD);
			log.debug("password: [for security reasons is not displayed but is it set?"
					+ TextUtils.stringSet(password) + "]");
			String protocolKeyFile = props
					.getProperty(WebConfigurationProvider.PROTOCOL_KEY_FILE);
			log.debug("protocol key file: " + protocolKeyFile);
			String protocolPassPhrase = props
					.getProperty(WebConfigurationProvider.PROTOCOL_PASS_PHRASE);
			log.debug("protocol pass phrase: " + protocolPassPhrase);
			String protocolPortNumber = props
					.getProperty(WebConfigurationProvider.PROTOCOL_PORT_NUMBER);
			log.debug("protocol port number: " + protocolPortNumber);

			WebConfigurationProvider webConf = new WebConfigurationProvider();
			webConf.setParameters(props);
			ConfigurationProvider conf = new ConfigurationProvider(webConf);
			ProtocolsConfiguration protocols = SWCUtils
					.buildProtocolFromConfiguration(conf);

			final ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(username, password, protocols);

			log.debug("SVNKit authentication manager created");
			repository.setAuthenticationManager(authManager);

			log.debug("SVNKit authentication manager set to the repository");

			log.debug("Retrieving the root url from the Subversion server...");
			SVNURL root = repository.getRepositoryRoot(true);
			res = root.toString();
			log.debug("The root url is: " + res);
		} finally {
			if (repository != null)
				repository.closeSession();
		}
		return res;
	}

	public void connect(UserCredentials credentials, String id, String url)
			throws DataProviderException {
		try {
			this.id = id;
			this.userCredentials = credentials;
			if (id != null) {
				this.repository = SVNRepositoryPool.getInstance(id)
						.getRepository(credentials);
			}
			if (ConfigurationProvider.isMultiRepositoryMode()) {
				SVNURL s = this.repository.getRepositoryRoot(true);
				if (url.equals(s.toDecodedString())) {
					this.repository.setLocation(SVNURL.parseURIDecoded(url),
							false);
				} else if (url.indexOf(s.toDecodedString() + "/") != -1) {
					this.repository.setLocation(SVNURL.parseURIDecoded(url),
							false);
				} else {
					throw new DataProviderException(
							"Incorrect location:\nURL = '" + url
									+ "'\nRoot = '" + s.toDecodedString() + "'");
				}
			}

		} catch (Exception e) {
			throw new DataProviderException(e);
		}
	}

	// Only decreases the public SVNRepository active users count... and the
	// comments cache?
	public void close() throws DataProviderException {
		if (this.repository != null) {
			ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
					.getSVNAuthenticationManager(null, null, null);
			repository.setAuthenticationManager(authManager);
		}
		org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool.getInstance(
				this.id).releaseRepository(this.repository);
	}

	public long getHeadRevision() throws DataProviderException {
		try {
			return this.repository.getLatestRevision();
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			throw new DataProviderException(e);
		}
	}

	public DataDirectoryElement getDirectory(String url, long revision)
			throws DataProviderException {
		return this.getDirectory(url, revision, false);
	}

	public DataDirectoryElement getDirectory(String url, long revision,
			boolean recusive) throws DataProviderException {
		try {
			DataDirectoryElement directory = new DataDirectoryElement();

			List directoryEntries = new ArrayList();
			SVNDirEntry directoryEntry = this.repository.getDir(url, revision,
					false, directoryEntries);

			directory.setName(directoryEntry.getName());
			directory.setRevision(directoryEntry.getRevision());
			directory.setAuthor(directoryEntry.getAuthor());
			directory.setDate(directoryEntry.getDate());
			directory.setComment(CommentsCache.getInstance(this.id,
					this.getVerifiedUserName(), this.getVerifiedPassword())
					.getComment(directoryEntry.getRevision()));

			List childElements = new ArrayList();
			for (Iterator i = directoryEntries.iterator(); i.hasNext();) {
				SVNDirEntry childEntry = (SVNDirEntry) i.next();
				DataRepositoryElement childElement;
				if (childEntry.getKind() == SVNNodeKind.DIR) {
					childElement = new DataDirectoryElement();
				} else {
					childElement = new DataFileElement();
					childElement.setSize(childEntry.getSize());
				}
				childElement.setName(childEntry.getName());
				childElement.setRevision(childEntry.getRevision());

				if (childEntry.getDate() == null) {
					try {
						String childUrl = null;
						if ("".equals(url)) {
							childUrl = childEntry.getName();
						} else {
							childUrl = url + "/" + childEntry.getName();
						}

						DataRepositoryElement el = this.getInfo(childUrl,
								revision);
						if (el != null) {
							childElement.setAuthor(el.getAuthor());
							childElement.setDate(el.getDate());
						}
					} catch (Exception ae) {
						// ignore
					}
				} else {
					childElement.setAuthor(childEntry.getAuthor());
					childElement.setDate(childEntry.getDate());
				}

				childElement.setComment(CommentsCache.getInstance(this.id,
						this.getVerifiedUserName(), this.getVerifiedPassword())
						.getComment(childEntry.getRevision()));
				childElements.add(childElement);

				if (recusive) {
					String childUrl = url + "/" + childElement.getName();
					DataDirectoryElement child = this.getDirectory(childUrl,
							revision, true);
					for (Iterator j = child.getChildElements().iterator(); j
							.hasNext();) {
						DataRepositoryElement nested = (DataRepositoryElement) j
								.next();
						nested.setName(childElement.getName() + "/"
								+ nested.getName());
						childElements.add(nested);
					}
				}
			}

			directory.setChildElements(childElements);
			return directory;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public static void terminate(String id) throws DataProviderException {
		log.debug("Terminating the repository id=" + id + "...");

		org.polarion.svncommons.commentscache.CommentsCache.shutdown(id);
		log.debug("org.polarion.svncommons.commentscache.CommentsCache shutted down");
		org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool.terminate(id);
		log.debug("org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool terminated");
		org.polarion.svncommons.commentscache.SVNRepositoryPool.terminate(id);
		log.debug("org.polarion.svncommons.commentscache.SVNRepositoryPool terminated");

		log.debug("...the repository id=" + id + " has been terminated");
	}

	public static void terminate() {
		org.polarion.svncommons.commentscache.CommentsCache.shutdown();
		org.polarion.svnwebclient.data.javasvn.SVNRepositoryPool.terminate();
		org.polarion.svncommons.commentscache.SVNRepositoryPool.terminate();

	}

	public List getRevisions(String url, long fromRevision, long toRevision,
			long count) throws DataProviderException {
		try {
			List ret = new ArrayList();

			final List logEntries = new ArrayList();
			if (count > 0) {
				this.repository.log(new String[] { url }, fromRevision,
						toRevision, false, false, count,
						new ISVNLogEntryHandler() {
							public void handleLogEntry(SVNLogEntry logEntry) {
								logEntries.add(logEntry);
							}
						});
			} else {
				this.repository.log(new String[] { url }, fromRevision,
						toRevision, false, false, new ISVNLogEntryHandler() {
							public void handleLogEntry(SVNLogEntry logEntry) {
								logEntries.add(logEntry);
							}
						});
			}

			for (Iterator i = logEntries.iterator(); i.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) i.next();
				DataRevision revision = new DataRevision();
				revision.setRevision(logEntry.getRevision());
				revision.setAuthor(logEntry.getAuthor());
				revision.setDate(logEntry.getDate());
				revision.setComment(CommentsCache.getInstance(this.id,
						this.getVerifiedUserName(), this.getVerifiedPassword())
						.getComment(logEntry.getRevision()));
				ret.add(revision);
			}

			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", fromRevision: " + fromRevision
					+ ", toRevision: " + toRevision + ", count: " + count;
			throw new IncorrectParameterException(message, description);
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public void setRelativeLocation(String id, String reposName)
			throws DataProviderException {
		this.reposLocation = this.getLocation(id, reposName);
	}

	public String getLocation(String url, long pegRevision, long revision)
			throws DataProviderException {
		try {
			Collection locations = this.repository.getLocations(url,
					(Collection) null, pegRevision, new long[] { revision });
			if (locations.size() == 1) {
				String location = ((SVNLocationEntry) locations.iterator()
						.next()).getPath();
				if (location.startsWith("/")) {
					if (location.length() > 1) {
						location = location.substring(1, location.length());
					} else {
						location = "";
					}
				}
				return this.normalizeLocation(location);
			} else {
				throw new DataProviderException("Unable to find location in "
						+ revision + " revision of " + url + " in revision "
						+ pegRevision);
			}
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision
					+ ", pegRevision: " + pegRevision;
			throw new IncorrectParameterException(message, description);
		}
	}

	protected String normalizeLocation(String location) {
		int index = location.indexOf(this.reposLocation);
		if (index == 0) {
			if (location.equals(this.reposLocation)) {
				location = "";
			} else {
				location = location.substring(this.reposLocation.length());
				if (location.startsWith("/") && location.length() > 1) {
					location = location.substring(1);
				}
			}
		}
		return location;
	}

	protected String getLocation(String id, String repositoryName)
			throws DataProviderException {
		String res = null;
		try {
			if (ConfigurationProvider.getInstance(id).isMultiRepositoryMode()) {
				res = repositoryName;
			} else {
				String url = ConfigurationProvider.getInstance(id)
						.getRepositoryUrl();
				if (url.startsWith(id)) {
					if (url.length() == id.length()) {
						res = id;
					} else {
						res = url.substring(id.length() + 1);
					}
				} else {
					throw new DataProviderException("Repository url - " + url
							+ " and Id - " + id + " are not compatible");
				}
			}
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
		return res;
	}

	/**
	 * if there's no entry with at the specified path, throw
	 * IncorrectParameterException
	 */
	public DataRepositoryElement getInfo(String url, long revision)
			throws DataProviderException {
		try {
			DataRepositoryElement ret;
			SVNDirEntry entry = this.repository.info(url, revision);
			if (entry == null) {
				String description = "HTTP Path Not Found";
				String message = "Url: " + url + ", revision: " + revision;
				throw new IncorrectParameterException(message, description);
			} else {
				if (SVNNodeKind.DIR == entry.getKind()) {
					ret = new DataDirectoryElement();
				} else {
					ret = new DataFileElement();
					ret.setSize(entry.getSize());
				}
				ret.setName(entry.getName());
				ret.setRevision(entry.getRevision());
				ret.setAuthor(entry.getAuthor());
				ret.setDate(entry.getDate());
				ret.setComment(CommentsCache.getInstance(id,
						this.getVerifiedUserName(), this.getVerifiedPassword())
						.getComment(entry.getRevision()));
				return ret;
			}
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			throw new DataProviderException();
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public DataFileElement getFile(String url, long revision,
			String containerMimeType) throws DataProviderException {
		try {
			DataFileElement file = (DataFileElement) this
					.getInfo(url, revision);
			SVNProperties props = new SVNProperties();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			this.repository.getFile(url, revision, props, outputStream);
			file.setContent(outputStream.toByteArray());

			String mimeType = props
					.getStringValue(SVNProperty.MIME_TYPE);
			if (mimeType == null) {
				mimeType = containerMimeType;
			}
			if (mimeType == null) {
				mimeType = SVNFileUtil.detectMimeType(new ByteArrayInputStream(
						file.getContent()));
			}

			if (mimeType == null) {
				file.setBinary(false);
			} else {
				if (ConfigurationProvider.getInstance(id).getBinaryMimeTypes()
						.contains(mimeType)) {
					file.setBinary(true);
				} else if (ConfigurationProvider.getInstance(id)
						.getTextMimeTypes().contains(mimeType)) {
					file.setBinary(false);
				} else {
					if (DataProvider.MIME_TYPE_BINARY
							.equalsIgnoreCase(mimeType)) {
						file.setBinary(true);
					} else {
						file.setBinary(false);
					}
				}
			}

			return file;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		} catch (IOException ex) {
			throw new DataProviderException(ex);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public DataFile getFileData(String url, long revision,
			String containerMimeType) throws DataProviderException {
		try {
			DataFile file = new DataFile();
			SVNProperties props = new SVNProperties();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			this.repository.getFile(url, revision, props, outputStream);
			file.setContent(outputStream.toByteArray());

			String mimeType = props
					.getStringValue(SVNProperty.MIME_TYPE);
			if (mimeType == null) {
				mimeType = containerMimeType;
			}
			if (mimeType == null) {
				mimeType = SVNFileUtil.detectMimeType(new ByteArrayInputStream(
						file.getContent()));
			}

			if (mimeType == null) {
				file.setBinary(false);
			} else {
				if (ConfigurationProvider.getInstance(id).getBinaryMimeTypes()
						.contains(mimeType)) {
					file.setBinary(true);
				} else if (ConfigurationProvider.getInstance(id)
						.getTextMimeTypes().contains(mimeType)) {
					file.setBinary(false);
				} else {
					if (DataProvider.MIME_TYPE_BINARY
							.equalsIgnoreCase(mimeType)) {
						file.setBinary(true);
					} else {
						file.setBinary(false);
					}
				}
			}
			return file;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		} catch (IOException ex) {
			throw new DataProviderException(ex);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public List getAnnotation(String url, long revision, String encoding)
			throws DataProviderException {
		try {
			final List ret = new ArrayList();
			File tempDirectory = new File(
					ConfigurationProvider.getTempDirectory());
			tempDirectory.mkdirs();
			SVNAnnotationGenerator generator = new SVNAnnotationGenerator(url,
					tempDirectory, 1, new ISVNEventHandler() {
						public void handleEvent(SVNEvent event, double progress) {
						}

						public void checkCancelled() throws SVNCancelException {
						}
					});

			this.repository.getFileRevisions(url, 1, revision, generator);
			generator.reportAnnotations(new ISVNAnnotateHandler() {
				public void handleLine(Date date, long revision, String author,
						String line) {
					DataAnnotationElement annotationElement = new DataAnnotationElement(
							revision, date, author, line);
					ret.add(annotationElement);
				}

				public void handleLine(Date date, long revision, String author,
						String line, Date mergedDate, long mergedRevision,
						String mergedAuthor, String mergedPath, int lineNumber)
						throws SVNException {
					DataAnnotationElement annotationElement = new DataAnnotationElement(
							revision, date, author, line);
					ret.add(annotationElement);
				}

				public void handleEOF() {
				}

				public boolean handleRevision(Date date, long revision,
						String author, File contents) throws SVNException {
					return false;
				}
			}, encoding);

			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		}
	}

	public DataRevision getRevisionInfo(long revision)
			throws DataProviderException {
		try {
			final DataRevision ret = new DataRevision();
			ret.setRevision(revision);
			this.repository.log(new String[] { "" }, revision, revision, true,
					false, 1, new ISVNLogEntryHandler() {
						public void handleLogEntry(SVNLogEntry logEntry)
								throws SVNException {
							try {
								ret.setAuthor(logEntry.getAuthor());
								ret.setDate(logEntry.getDate());
								ret.setComment(CommentsCache.getInstance(id,
										getVerifiedUserName(),
										getVerifiedPassword()).getComment(
										logEntry.getRevision()));

								Map changes = logEntry.getChangedPaths();
								for (Iterator i = changes.entrySet().iterator(); i
										.hasNext();) {
									Map.Entry entry = (Map.Entry) i.next();
									SVNLogEntryPath value = (SVNLogEntryPath) entry
											.getValue();
									String copyPath = value.getCopyPath();
									long copyRevision = value.getCopyRevision();

									String path = value.getPath();
									if (path.startsWith("/")
											&& (path.length() > 1)) {
										path = path.substring(1);
									}
									path = normalizeLocation(path);
									if (copyPath != null) {
										copyPath = normalizeLocation(copyPath);
									}
									char type = value.getType();
									if ('A' == type) {
										ret.addChangedElement(
												DataRevision.TYPE_ADDED, path,
												copyPath, copyRevision);
									} else if ('D' == type) {
										ret.addChangedElement(
												DataRevision.TYPE_DELETED,
												path, copyPath, copyRevision);
									} else if ('M' == type) {
										ret.addChangedElement(
												DataRevision.TYPE_MODIFIED,
												path, copyPath, copyRevision);
									} else {
										ret.addChangedElement(
												DataRevision.TYPE_REPLACED,
												path, copyPath, copyRevision);
									}
								}
							} catch (Exception e) {
								throw new SVNException(
										SVNErrorMessage.UNKNOWN_ERROR_MESSAGE,
										e);
							}
						}
					});
			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Revision: " + revision;
			throw new IncorrectParameterException(message, description);
		}
	}

	/**
	 * Cheap detection of file type - only be mime type property. This property
	 * must be set by SVN client
	 * 
	 * @param url
	 * @param revision
	 * @return
	 * @throws DataProviderException
	 */
	public boolean isBinaryFile(String url, long revision,
			String containerMimeType) throws DataProviderException {
		try {
			boolean ret;

			SVNProperties props = new SVNProperties();
			this.repository.getFile(url, revision, props, null);
			String mimeType = props
					.getStringValue(SVNProperty.MIME_TYPE);
			if (mimeType == null) {
				mimeType = containerMimeType;
			}

			if (mimeType == null) {
				ret = false;
			} else {
				if (ConfigurationProvider.getInstance(id).getBinaryMimeTypes()
						.contains(mimeType)) {
					ret = true;
				} else if (ConfigurationProvider.getInstance(id)
						.getTextMimeTypes().contains(mimeType)) {
					ret = false;
				} else {
                    ret = DataProvider.MIME_TYPE_BINARY
                            .equalsIgnoreCase(mimeType);
				}
			}

			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public void testConnection() throws AuthenticationException {
		try {
			this.repository.testConnection();
		} catch (SVNException e) {
			throw new AuthenticationException(e);
		}
	}

	public DataChangedElement createDirectory(String url, String name,
			String comment) throws DataProviderException {
		try {
			DataChangedElement ret = new DataChangedElement();
			SVNCommitClient commitClient = new SVNCommitClient(
					this.repository.getAuthenticationManager(),
					new DefaultSVNOptions());
			SVNCommitInfo commitInfo = null;
			if (isCompoundDir(name)) {
				commitInfo = this.createCompoundDir(url, name, comment,
						commitClient);
			} else {
				SVNURL absoluteUrl = this.repository.getLocation();
				absoluteUrl = absoluteUrl.appendPath(url, false);
				absoluteUrl = absoluteUrl.appendPath(name, false);
				commitInfo = commitClient.doMkDir(new SVNURL[] { absoluteUrl },
						comment);
			}

			ret.setName(name);
			ret.setRevision(commitInfo.getNewRevision());
			ret.setAuthor(commitInfo.getAuthor());
			ret.setDate(commitInfo.getDate());
			ret.setComment(CommentsCache.getInstance(this.id,
					this.getVerifiedUserName(), this.getVerifiedPassword())
					.getComment(commitInfo.getNewRevision()));
			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", name: " + name;
			throw new IncorrectParameterException(message, description);
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	protected SVNCommitInfo createCompoundDir(String url, String name,
			String comment, SVNCommitClient client)
			throws DataProviderException {
		SVNCommitInfo commitInfo = null;
		File tempDirForImport = this.getTempDirForImport(name);
		String strDirForImport = tempDirForImport.getAbsolutePath();
		try {
			SVNURL absoluteUrl = this.repository.getLocation();
			absoluteUrl = absoluteUrl.appendPath(url, false);

			String tmpPaths[] = null;
			int index = name.indexOf("/");
			if (index != -1) {
				tmpPaths = name.split("/");
			} else {
				tmpPaths = name.split("\\\\");
			}
			List paths = new ArrayList();
			for (int i = 0; i < tmpPaths.length; i++) {
				paths.add(tmpPaths[i]);
			}

			while (true) {
				try {
					commitInfo = client.doImport(tempDirForImport, absoluteUrl,
							comment, true);
					break;
				} catch (SVNAuthenticationException sa) {
					throw sa;
				} catch (SVNException se) {
					if (paths.size() > 0) {
						String prefix = (String) paths.remove(0);
						absoluteUrl = absoluteUrl.appendPath(prefix, false);
						tempDirForImport = new File(
								tempDirForImport.getAbsolutePath(), prefix);
					}
				}
			}

		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			throw new DataProviderException(e);
		} finally {
			if (tempDirForImport != null) {
				FileUtil.deleteDirectory(new File(strDirForImport));
			}
		}
		return commitInfo;
	}

	protected File getTempDirForImport(String name) {
		String temporaryDirectory = ConfigurationProvider.getTempDirectory();
		String destinationDirectory = temporaryDirectory + "/" + UUID.getUUID();
		File res = new File(destinationDirectory, name);
		res.mkdirs();

		int index = name.indexOf("/");
		if (index == -1) {
			index = name.indexOf("\\");
		}
		return new File(destinationDirectory);
	}

	protected boolean isCompoundDir(String path) {
		return (path.indexOf("/") != -1 || path.indexOf("\\") != -1);
	}

	public DataChangedElement addFile(String url, String path, String comment)
			throws DataProviderException {
		try {
			DataChangedElement ret = new DataChangedElement();
			SVNCommitClient commitClient = new SVNCommitClient(
					this.repository.getAuthenticationManager(),
					new DefaultSVNOptions());
			File file = new File(path);
			String filename = file.getName();
			if (file.exists()) {
				SVNURL absoluteUrl = this.repository.getLocation();
				absoluteUrl = absoluteUrl.appendPath(url, false);
				absoluteUrl = absoluteUrl.appendPath(filename, false);
				SVNCommitInfo commitInfo = commitClient.doImport(file,
						absoluteUrl, comment, false);
				ret.setName(filename);
				ret.setRevision(commitInfo.getNewRevision());
				ret.setAuthor(commitInfo.getAuthor());
				ret.setDate(commitInfo.getDate());
				ret.setComment(CommentsCache.getInstance(this.id,
						this.getVerifiedUserName(), this.getVerifiedPassword())
						.getComment(commitInfo.getNewRevision()));
			} else {
				throw new DataProviderException(filename
						+ " could not be found");
			}
			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", path: " + path;
			throw new IncorrectParameterException(message, description);
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public DataChangedElement delete(String url, List elements, String comment)
			throws DataProviderException {
		try {
			DataChangedElement ret = new DataChangedElement();
			SVNCommitClient commitClient = new SVNCommitClient(
					this.repository.getAuthenticationManager(),
					new DefaultSVNOptions());
			SVNURL absoluteUrl = this.repository.getLocation();
			absoluteUrl = absoluteUrl.appendPath(url, false);
			SVNURL[] deletedElements = new SVNURL[elements.size()];
			for (int i = 0; i < elements.size(); i++) {
				deletedElements[i] = absoluteUrl.appendPath(
						(String) elements.get(i), false);
			}
			SVNCommitInfo commitInfo = commitClient.doDelete(deletedElements,
					comment);
			ret.setName(UrlUtil.getLastPathElement(url));
			ret.setRevision(commitInfo.getNewRevision());
			ret.setAuthor(commitInfo.getAuthor());
			ret.setDate(commitInfo.getDate());
			ret.setComment(CommentsCache.getInstance(this.id,
					this.getVerifiedUserName(), this.getVerifiedPassword())
					.getComment(commitInfo.getNewRevision()));
			return ret;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url;
			throw new IncorrectParameterException(message, description);
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		} catch (ConfigurationException e) {
			throw new DataProviderException(e);
		}
	}

	public DataChangedElement commitFile(String url, String path, String comment)
			throws DataProviderException {
		InputStream fileReader = null;
		try {
			DataChangedElement ret = new DataChangedElement();
			File file = new File(path);
			String filename = file.getName();
			if (file.exists()) {
				long size = file.length();
				fileReader = new FileInputStream(file);

				/*
				 * We need to change repository location to point to file's
				 * parent directory in order to avoid possible authorization
				 * problem, where user has restricted access to repository
				 * location but has full access to current directory.
				 */
				SVNURL repositoryUrl = this.repository.getLocation();
				try {
					SVNURL svnUrl = repositoryUrl.appendPath(
							UrlUtil.getPreviousFullPath(url), false);
					this.repository.setLocation(svnUrl, false);

					String dirUrl = "";
					String fileUrl = UrlUtil.getLastPathElement(url);

					ISVNEditor editor = repository.getCommitEditor(comment,
							new WorkspaceMediator());
					SVNCommitInfo commitInfo = SVNUtils.modifyFile(editor,
							dirUrl, fileUrl, fileReader, size);
					ret.setName(filename);
					ret.setRevision(commitInfo.getNewRevision());
					ret.setAuthor(commitInfo.getAuthor());
					ret.setDate(commitInfo.getDate());
					ret.setComment(CommentsCache.getInstance(this.id,
							this.getVerifiedUserName(),
							this.getVerifiedPassword()).getComment(
							commitInfo.getNewRevision()));
					return ret;
				} finally {
					this.repository.setLocation(repositoryUrl, false);
				}
			} else {
				throw new DataProviderException(filename
						+ " could not be found");
			}
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException se) {
			String description = this.checkExceptionType(se);
			String message = "Url: " + url + ", path: " + path;
			throw new IncorrectParameterException(message, description);
		} catch (Exception e) {
			throw new DataProviderException(e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public List compareDirectoryRevisions(String url, long startRevision,
			long endRevision) throws DataProviderException {
		try {
			Reporter reporter = new Reporter(startRevision);
			Editor editor = new Editor();

			SVNURL repositoryUrl = this.repository.getLocation();
			try {
				this.repository.setLocation(
						repositoryUrl.appendPath(url, false), false);
				this.repository.status(endRevision, null, true, reporter,
						editor);
			} finally {
				this.repository.setLocation(repositoryUrl, false);
			}

			List items = editor.getChangedItems();
			for (Iterator i = items.iterator(); i.hasNext();) {
				DataDirectoryCompareItem item = (DataDirectoryCompareItem) i
						.next();
				if (DataDirectoryCompareItem.OPERATION_DELETE == item
						.getOperation()) {
					String entryName = null;
					if ("".equals(url)) {
						entryName = item.getPath();
					} else {
						entryName = url + "/" + item.getPath();
					}
					SVNDirEntry entry = this.repository.info(entryName,
							startRevision);
					item.setDirectory(SVNNodeKind.DIR.equals(entry.getKind()));
					item.setOldRevision(entry.getRevision());
					item.setNewRevision(-1);
				}
			}
			return items;
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", startRevision: "
					+ startRevision + ", endRevision: " + endRevision;
			throw new IncorrectParameterException(message, description);
		}
	}

	public String getFileDifference(String url, long startRevision,
			long endRevision, String pathToStart, String pathToEnd,
			String encoding) throws DataProviderException {
		try {
			ISVNDiffGenerator diffGenerator = new DefaultSVNDiffGenerator();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			diffGenerator.setEncoding(encoding);
			diffGenerator.displayFileDiff(url, new File(pathToStart), new File(
					pathToEnd), Long.toString(startRevision), Long
					.toString(endRevision), null, null, os);
			return os.toString(encoding);
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException se) {
			String description = this.checkExceptionType(se);
			String message = "Url: " + url + ", : startRevision"
					+ startRevision + ", endRevision: " + endRevision;
			throw new IncorrectParameterException(message, description);
		} catch (Exception e) {
			throw new DataProviderException(e);
		}
	}

	public int checkUrl(String url, long revision) throws DataProviderException {
		try {
			SVNNodeKind result = this.repository.checkPath(url, revision);
			if (SVNNodeKind.NONE.equals(result)) {
				return IDataProvider.NOT_EXIST;
			} else if (SVNNodeKind.DIR.equals(result)) {
				return IDataProvider.DIRECTORY;
			} else if (SVNNodeKind.FILE.equals(result)) {
				return IDataProvider.FILE;
			} else {
				return IDataProvider.UNKNOWN;
			}
		} catch (SVNAuthenticationException ae) {
			throw new AuthenticationException(ae);
		} catch (SVNException e) {
			String description = this.checkExceptionType(e);
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		}
	}

	protected String getVerifiedUserName() throws ConfigurationException {
		if (ConfigurationProvider.getInstance(id).isMultiRepositoryMode()) {
			return this.userCredentials.getUsername();
		} else {
			return ConfigurationProvider.getInstance(id).getUsername();
		}
	}

	protected String getVerifiedPassword() throws ConfigurationException {
		if (ConfigurationProvider.getInstance(id).isMultiRepositoryMode()) {
			return this.userCredentials.getPassword();
		} else {
			return ConfigurationProvider.getInstance(id).getPassword();
		}
	}

	protected String checkExceptionType(SVNException se)
			throws DataProviderException {
		String res = null;
		SVNErrorCode ourErrorCode = se.getErrorMessage().getErrorCode();
		if (ourErrorCode.equals(SVNErrorCode.RA_DAV_PATH_NOT_FOUND)
				|| ourErrorCode.equals(SVNErrorCode.FS_NO_SUCH_REVISION)
				|| ourErrorCode.equals(SVNErrorCode.FS_NOT_FOUND)) {
			res = ourErrorCode.getDescription();
		} else {
			throw new DataProviderException(se);
		}
		return res;
	}

	@Override
	public String getId() {
		return id;
	}
}
