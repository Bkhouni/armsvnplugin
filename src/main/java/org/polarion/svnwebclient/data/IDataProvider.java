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
package org.polarion.svnwebclient.data;

import org.polarion.svnwebclient.authorization.UserCredentials;
import org.polarion.svnwebclient.data.model.*;

import java.util.List;

/**
 * Interface to SVN repository
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public interface IDataProvider {
	int NOT_EXIST = 0;
	int FILE = 1;
	int DIRECTORY = 2;
	int UNKNOWN = 3;

	void connect(UserCredentials credentials, String id, String url)
			throws DataProviderException;

	void close() throws DataProviderException;

	long getHeadRevision() throws DataProviderException;

	DataDirectoryElement getDirectory(String url, long revision)
			throws DataProviderException;

	DataDirectoryElement getDirectory(String url, long revision,
			boolean recusive) throws DataProviderException;

	List getRevisions(String url, long fromRevision, long toRevision, long count)
			throws DataProviderException;

	String getLocation(String url, long pegRevision, long revision)
			throws DataProviderException;

	DataRepositoryElement getInfo(String url, long revision)
			throws DataProviderException;

	DataFileElement getFile(String url, long revision, String containerMimeType)
			throws DataProviderException;

	DataFile getFileData(String url, long revision, String containerMimeType)
			throws DataProviderException;

	List getAnnotation(String url, long revision, String encoding)
			throws DataProviderException;

	DataRevision getRevisionInfo(long revision) throws DataProviderException;

	boolean isBinaryFile(String url, long revision, String containerMimeType)
			throws DataProviderException;

	void testConnection() throws AuthenticationException;

	DataChangedElement createDirectory(String url, String name, String comment)
			throws DataProviderException;

	/**
	 * Adding new file to repository
	 * 
	 * @param url
	 *            Url relative to repository location Note: url doesn't include
	 *            file name. Example: test file name is retrieved from path.
	 *            Example: task.txt
	 * @param path
	 *            Full path on file system to file
	 * @param comment
	 * @return
	 * @throws DataProviderException
	 */
	DataChangedElement addFile(String url, String path, String comment)
			throws DataProviderException;

	DataChangedElement delete(String url, List elements, String comment)
			throws DataProviderException;

	DataChangedElement commitFile(String url, String path, String comment)
			throws DataProviderException;

	List compareDirectoryRevisions(String url, long startRevision,
			long endRevision) throws DataProviderException;

	String getFileDifference(String url, long startRevision, long endRevision,
			String pathToStart, String pathToEnd, String encoding)
			throws DataProviderException;

	int checkUrl(String url, long revision) throws DataProviderException;

	void setRelativeLocation(String id, String url)
			throws DataProviderException;

	String getId();
}
