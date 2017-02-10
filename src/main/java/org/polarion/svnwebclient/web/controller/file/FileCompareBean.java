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
package org.polarion.svnwebclient.web.controller.file;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataFile;
import org.polarion.svnwebclient.data.model.DataRevision;
import org.polarion.svnwebclient.util.FileUtil;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.util.contentencoding.ContentEncodingHelper;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.RevisionDetails;
import org.polarion.svnwebclient.web.model.data.file.FileCompareInfo;
import org.polarion.svnwebclient.web.model.data.file.FileCompareResult;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.*;
import org.w3c.util.UUID;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class FileCompareBean extends AbstractBean {
	public static final String START_REVISION_CONTENT = "startrevision";
	public static final String END_REVISION_CONTENT = "endrevision";
	public static final String START_REVISION_BINARY = "startrevisionbinary";
	public static final String END_REVISION_BINARY = "endrevisionbinary";
	public static final String PARAM_EXTENSION = "extension";

	public static int ADD = 0;
	public static int DEL = 1;
	public static int MOD = 2;

	protected long headRevision;
	protected long revision;
	protected DataRevision startRevision;
	protected DataRevision endRevision;
	protected String extension;
	protected IDataProvider dataProvider;

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		this.setExtension();

		if (this.requestHandler.getPegRevision() != -1) {
			this.revision = this.requestHandler.getPegRevision();
		} else if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		} else {
			this.revision = this.headRevision;
		}

		String tempDirectoryPath = ConfigurationProvider.getTempDirectory()
				+ "/" + UUID.getUUID();
		try {
			File tempDirectory = new File(tempDirectoryPath);
			if (!tempDirectory.exists()) {
				tempDirectory.mkdirs();
			}

			DataFile startRevisionData = null;
			DataFile endRevisionData = null;
			boolean isBinary = false;

			if (this.requestHandler.getStartRevision() != -1) {
				this.startRevision = dataProvider
						.getRevisionInfo(this.requestHandler.getStartRevision());
				String startRevisionLocation = this.requestHandler.getUrl();
				if (this.requestHandler.getEndRevision() != -1) {
					// if file was deleted we could'n know its location, because
					// it doesn't present in current revision
					// so in this case we think that location is correct
					startRevisionLocation = dataProvider.getLocation(
							this.requestHandler.getUrl(), this.revision,
							this.requestHandler.getStartRevision());
				}
				String fileName = UrlUtil
						.getLastPathElement(startRevisionLocation);
				String containerMimeType = this.state.getSession()
						.getServletContext()
						.getMimeType(fileName.toLowerCase());

				startRevisionData = dataProvider.getFileData(
						startRevisionLocation,
						this.requestHandler.getStartRevision(),
						containerMimeType);
				isBinary = startRevisionData.isBinary();
			}

			if (this.requestHandler.getEndRevision() != -1) {
				this.endRevision = dataProvider
						.getRevisionInfo(this.requestHandler.getEndRevision());
				if (!isBinary) {
					String endRevisionLocation = dataProvider.getLocation(
							this.requestHandler.getUrl(), this.revision,
							this.requestHandler.getEndRevision());

					String fileName = UrlUtil
							.getLastPathElement(endRevisionLocation);
					String containerMimeType = this.state.getSession()
							.getServletContext()
							.getMimeType(fileName.toLowerCase());

					endRevisionData = dataProvider.getFileData(
							endRevisionLocation,
							this.requestHandler.getEndRevision(),
							containerMimeType);
					isBinary = endRevisionData.isBinary();
				}
			}
			// encoding
			String encoding = ContentEncodingHelper.getEncoding(
					dataProvider.getId(), this.state);
			if (!isBinary) {
				if ((startRevisionData != null) && (endRevisionData != null)) {
					String startPath = tempDirectoryPath + "/" + UUID.getUUID();
					this.writeFile(startPath, startRevisionData.getContent());
					String endPath = tempDirectoryPath + "/" + UUID.getUUID();
					this.writeFile(endPath, endRevisionData.getContent());

					String difference = dataProvider.getFileDifference(
							this.requestHandler.getUrl(),
							this.requestHandler.getStartRevision(),
							this.requestHandler.getEndRevision(), startPath,
							endPath, encoding);
					DifferenceModel model = new DifferenceModel(difference);

					String startRevisionContent = new String(
							startRevisionData.getContent(), encoding);
					String endRevisionContent = new String(
							endRevisionData.getContent(), encoding);

					this.state
							.getRequest()
							.getSession()
							.setAttribute(
									FileCompareBean.START_REVISION_CONTENT,
									model.getLeftLines(startRevisionContent));
					this.state
							.getRequest()
							.getSession()
							.setAttribute(FileCompareBean.END_REVISION_CONTENT,
									model.getRightLines(endRevisionContent));
				} else {
					if (startRevisionData == null) {
						String endRevisionContent = new String(
								endRevisionData.getContent(), encoding);
						this.state
								.getRequest()
								.getSession()
								.setAttribute(
										FileCompareBean.END_REVISION_CONTENT,
										DifferenceModel
												.getUntouchedLines(endRevisionContent));
					} else if (endRevisionData == null) {
						String startRevisionContent = new String(
								startRevisionData.getContent(), encoding);
						this.state
								.getRequest()
								.getSession()
								.setAttribute(
										FileCompareBean.START_REVISION_CONTENT,
										DifferenceModel
												.getUntouchedLines(startRevisionContent));
					}
				}
			} else {
				this.state
						.getRequest()
						.getSession()
						.setAttribute(FileCompareBean.START_REVISION_BINARY,
								new Boolean(true));
				this.state
						.getRequest()
						.getSession()
						.setAttribute(FileCompareBean.END_REVISION_BINARY,
								new Boolean(true));
			}
		} catch (DataProviderException e) {
			throw e;
		} catch (Exception ex) {
			throw new SVNWebClientException(ex);
		} finally {
			FileUtil.deleteDirectory(new File(tempDirectoryPath));
		}

		return true;
	}

	public String getStartRevisionViewUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_COMPARE_DATA, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.STARTREV,
				Long.toString(this.requestHandler.getStartRevision()));
		urlGenerator.addParameter(FileCompareBean.PARAM_EXTENSION,
				this.extension);
		return urlGenerator.getUrl();
	}

	public String getEndRevisionViewUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_COMPARE_DATA, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.ENDREV,
				Long.toString(this.requestHandler.getEndRevision()));
		urlGenerator.addParameter(FileCompareBean.PARAM_EXTENSION,
				this.extension);
		return urlGenerator.getUrl();
	}

	public RevisionDetails getStartRevisionInfo() {
		if (this.startRevision == null) {
			return null;
		}
		return new RevisionDetails(this.startRevision, this.headRevision, null,
				null, null);
	}

	public RevisionDetails getEndRevisionInfo() {
		if (this.endRevision == null) {
			return null;
		}
		return new RevisionDetails(this.endRevision, this.headRevision, null,
				null, null);
	}

	public String getStartRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV,
				Long.toString(this.requestHandler.getStartRevision()));
		return urlGenerator.getUrl();
	}

	public String getEndRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV,
				Long.toString(this.requestHandler.getEndRevision()));
		return urlGenerator.getUrl();
	}

	protected void writeFile(String path, byte[] content) throws Exception {
		File file = new File(path);
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			stream.write(content);
		} finally {
			if (stream != null) {
				try {
					stream.flush();
				} catch (Exception e) {
				}

				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public String getCurrentUrlWithParameters() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_COMPARE, requestHandler.getLocation());

		Iterator iter = this.state.getRequest().getParameterMap().entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String paramName = (String) entry.getKey();

			if (!RequestParameters.CHARACTER_ENCODING.equals(paramName)) {
				String[] values = (String[]) entry.getValue();
				if (values != null && values.length > 0) {
					for (int i = 0; i < values.length; i++) {
						String paramValue = values[i];
						urlGenerator.addParameter(paramName, paramValue);
					}
				} else {
					urlGenerator.addParameter(paramName);
				}
			}
		}
		return urlGenerator.getUrl();
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(),
				this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), true);
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	public FileCompareInfo getChangeSummary() {
		List dataStart = (List) this.state.getRequest().getSession()
				.getAttribute(FileCompareBean.START_REVISION_CONTENT);
		List dataEnd = (List) this.state.getRequest().getSession()
				.getAttribute(FileCompareBean.END_REVISION_CONTENT);
		if (dataStart == null || dataEnd == null) {
			return null;
		}
		FileCompareInfo info = new FileCompareInfo();
		FileCompareResult startResult = new FileCompareResult(dataStart,
				this.extension);
		FileCompareResult endResult = new FileCompareResult(dataEnd,
				this.extension);
		Iterator itStart = startResult.getLines().iterator();
		Iterator itEnd = endResult.getLines().iterator();

		while (itStart.hasNext()) {
			FileCompareResult.Line startLine = (FileCompareResult.Line) itStart
					.next();
			FileCompareResult.Line endLine = (FileCompareResult.Line) itEnd
					.next();
			if (startLine.getChangeType() == DifferenceLine.MODIFIED) {
				this.stopPointsSettings(startLine, DifferenceLine.MODIFIED,
						FileCompareBean.MOD, info);
			} else if (endLine.getChangeType() == DifferenceLine.ADDED) {
				this.stopPointsSettings(endLine, DifferenceLine.ADDED,
						FileCompareBean.ADD, info);
			} else if (startLine.getChangeType() == DifferenceLine.DELETED) {
				this.stopPointsSettings(startLine, DifferenceLine.DELETED,
						FileCompareBean.DEL, info);
			}
		}
		return info;
	}

	public Collection getCharacterEncodings() throws ConfigurationException {
		return ContentEncodingHelper
				.getCharacterEncodings(dataProvider.getId());
	}

	public boolean isSelectedCharacterEncoding(String encoding)
			throws ConfigurationException {
		return ContentEncodingHelper.isSelectedCharacterEncoding(
				dataProvider.getId(), this.state, encoding);
	}

	protected void stopPointsSettings(FileCompareResult.Line line,
			int differenceType, int summaryType, FileCompareInfo info) {
		int number = new Integer(line.getNumber()).intValue();
		FileCompareInfo.StopPoints point = info.getLastProperElement(
				number - 1, summaryType);
		if (point != null) {
			point.setBlockPosition(number);
		} else {
			String url = summaryType == FileCompareBean.ADD ? this
					.getEndRevisionViewUrl() : this.getStartRevisionViewUrl();
			info.setStopPoint(number, url, summaryType, number);
			if (summaryType == FileCompareBean.ADD) {
				info.increaseAddedItemsCount();
			} else if (summaryType == FileCompareBean.DEL) {
				info.increaseDeletedItemsCount();
			} else if (summaryType == FileCompareBean.MOD) {
				info.increaseModifiedItemsCount();
			}
		}
	}

	protected void setExtension() {
		String url = this.requestHandler.getUrl();
		int index = url.lastIndexOf(".");
		if (index != -1) {
			this.extension = url.substring(index + 1);
		}
	}
}
