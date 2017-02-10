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
 * 
 */
package org.polarion.svncommons.commentscache.storage;

import org.apache.log4j.Logger;
import org.w3c.util.UUID;

import java.io.*;
import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileStorage {
	protected static final String INDEX_FILE = "index.txt";
	protected static final String REVISION_SPLITTER = "|";
	protected static final String LINE_SPLITTER = " _e-o-l_\n";
	protected static final String INDEX_VALUES_SPLITTER = "<->";
	protected static final String FILE_NAME_SPLITTER = "-";

	protected String url;
	protected String cacheStoragePath;
	protected String cacheDirectoryPath;
	protected long pageSize;

	protected Logger logger = Logger.getLogger(FileStorage.class);

	public FileStorage(String url, String cacheStoragePath, long pageSize)
			throws IOException {
		this.url = url;
		this.cacheStoragePath = cacheStoragePath;
		this.pageSize = pageSize;

		if (this.cacheStoragePath.endsWith("/")
				|| this.cacheStoragePath.endsWith("\\")) {
			this.cacheStoragePath = this.cacheStoragePath.substring(0,
					this.cacheStoragePath.length() - 1);
		}

		// check existence of cache storage directory and create if required
		File cacheStorage = new File(this.cacheStoragePath);
		if (!cacheStorage.exists()) {
			boolean created = cacheStorage.mkdirs();
			if (!created) {
				throw new IOException(this.cacheStoragePath
						+ " could not be created");
			}
		}

		// check existance of cache for URL
		// if cache does not exist define new location and save index
		Map index = this.readIndex(this.cacheStoragePath);
		String directoryName = (String) index.get(url);
		if (directoryName == null) {
			directoryName = UUID.getUUID();
			index.put(url, directoryName);
			this.writeIndex(this.cacheStoragePath, index);
		}

		this.cacheDirectoryPath = this.cacheStoragePath + "/" + directoryName;

		// create cache directory if does not exist
		File cacheDirectory = new File(this.cacheDirectoryPath);
		if (!cacheDirectory.exists()) {
			boolean created = cacheDirectory.mkdirs();
			if (!created) {
				throw new IOException(this.cacheDirectoryPath
						+ " could not be created");
			}
		}
	}

	/**
	 * Read index file content. Index file defines mapping of URL's to
	 * directories in cache
	 * 
	 * @param cacheStoragePath
	 *            Location of cache storage
	 * @return Index file content as map of URL-location
	 * @throws IOException
	 *             When index read fails
	 */
	protected Map readIndex(String cacheStoragePath) throws IOException {
		Map ret = new HashMap();
		BufferedReader input = null;
		try {
			File file = new File(this.getIndexFilename(cacheStoragePath));
			if (file.exists()) {
				input = new BufferedReader(new FileReader(file));
				String line;
				while ((line = input.readLine()) != null) {
					String[] values = line
							.split(FileStorage.INDEX_VALUES_SPLITTER);
					if (values.length == 2) {
						ret.put(values[0], values[1]);
					}
				}
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
				}
			}
		}
		return ret;
	}

	/**
	 * Write index file content. Index file defines mapping of URL's to
	 * directories in cache
	 * 
	 * @param cacheStoragePath
	 *            Location of cache storage
	 * @param index
	 *            Index file content as map of URL-location
	 * @throws IOException
	 *             When index write fails
	 */
	protected void writeIndex(String cacheStoragePath, Map index)
			throws IOException {
		BufferedWriter output = null;
		try {
			File indexFile = new File(this.getIndexFilename(cacheStoragePath));
			output = new BufferedWriter(new FileWriter(indexFile));
			Set keys = index.keySet();
			for (Iterator i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				String value = (String) index.get(key);

				output.write(key + FileStorage.INDEX_VALUES_SPLITTER + value);
				output.newLine();
			}
		} finally {
			if (output != null) {
				try {
					output.flush();
				} catch (Exception e) {
				}

				try {
					output.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Returns location of index file
	 * 
	 * @param cacheStoragePath
	 *            Path to cache storage
	 * @return Location of index file
	 */
	protected String getIndexFilename(String cacheStoragePath) {
		return cacheStoragePath + "/" + FileStorage.INDEX_FILE;
	}

	public synchronized Page loadPage(String pageName) throws IOException {
		Page res = null;

		String filename = this.cacheDirectoryPath + "/" + pageName;
		File file = new File(filename);

		BufferedInputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			byte[] byteContent = new byte[(int) file.length()];
			inputStream.read(byteContent);

			TreeMap data = new TreeMap();
			String content = new String(byteContent, "UTF-8");
			String[] records = content.split(FileStorage.LINE_SPLITTER);
			for (int i = 0; i < records.length; i++) {
				String record = records[i];
				int separator = record.indexOf(FileStorage.REVISION_SPLITTER);
				if (separator != -1) {
					String revision = record.substring(0, separator);
					String comment = record.substring(separator + 1);
					data.put(new Long(revision), comment);
				} else {
					this.logger.warn("Incorrect line format. Cache file: "
							+ file.getAbsolutePath() + ", line: " + record);
				}
			}

			long pageStartRevision = PageInfo.getPageStartRevision(pageName);
			long pageEndRevision = PageInfo.getPageEndRevision(pageName);

			res = new Page(pageStartRevision, pageEndRevision, data,
					this.pageSize);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

		// if file is empty (probably because of bad authentication), then
		// delete it in order it to be updated further
		if (res.getData().isEmpty()) {
			this.logger.warn("Deleting cache file because it's empty. File: "
					+ file.getAbsolutePath());
			file.delete();
		}

		return res;
	}

	public synchronized void savePage(Page page) throws IOException {
		String filename = this.cacheDirectoryPath + "/"
				+ page.getInfo().getPageName();

		OutputStreamWriter outputStream = null;
		try {
			outputStream = new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8");
			Set entries = page.getData().entrySet();
			for (Iterator i = entries.iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				String comment = (String) entry.getValue();
				if (comment == null) {
					comment = "";
				}

				StringBuffer line = new StringBuffer();
				line.append(entry.getKey());
				line.append(FileStorage.REVISION_SPLITTER);
				line.append(comment);
				line.append(FileStorage.LINE_SPLITTER);
				outputStream.write(line.toString());
			}
		} finally {
			if (outputStream != null) {
				try {
					outputStream.flush();
				} catch (Exception e) {
				}

				try {
					outputStream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public synchronized void deletePage(String pageName) throws IOException {
		String filename = this.cacheDirectoryPath + "/" + pageName;
		File file = new File(filename);
		boolean deleted = file.delete();
		if (!deleted) {
			throw new IOException(filename + " could not be deleted");
		}
	}

	public synchronized PageInfo check(long revision) {
		File cacheDirectory = new File(this.cacheDirectoryPath);
		File[] pageFiles = cacheDirectory.listFiles(new PageFileFilter(
				revision, this.pageSize));
		if ((pageFiles == null) || (pageFiles.length == 0)) {
			return null;
		} else {
			String pageName = pageFiles[0].getName();
			long pageStartRevision = PageInfo.getPageStartRevision(pageName);
			long pageEndRevision = PageInfo.getPageEndRevision(pageName);
			return new PageInfo(pageStartRevision, pageEndRevision,
					this.pageSize);
		}
	}

}
