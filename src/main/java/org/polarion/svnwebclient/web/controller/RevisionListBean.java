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
package org.polarion.svnwebclient.web.controller;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.data.model.DataRevision;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.model.Button;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.RevisionList;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class RevisionListBean extends AbstractBean {
	public static final String ADVANCED_NAVIGATION_ATTRIBUTE = "advancedNavigation";
	protected static final int SHOW_OTHER_MODE = 0;
	protected static final int SHOW_NEXT_MODE = 1;
	protected static final int SHOW_PREVIOUS_MODE = 2;

	protected long currentRevision;
	protected long headRevision;
	protected String url;
	protected List revisions = new ArrayList();
	protected DataRepositoryElement info;

	protected boolean isNextDisabled;
	protected boolean isPreviousDisabled;

	protected long startRevision;
	protected long endRevision;
	protected long revisionCount;
	protected boolean isReverseOrder;

	protected ButtonUrls buttonUrls = new ButtonUrls();

	protected int show_mode;
	protected long readRevisionCount;
	protected boolean isHidePolarionCommit;
	protected IDataProvider dataProvider;

	public class ButtonUrls {
		protected String next;
		protected String previous;
		protected long start;
		protected long end;

		protected long getNextStartRevision() {
			return this.start;
		}

		protected long getPreviousEndRevision() {
			return this.end;
		}

		protected UrlGenerator getUrlGenerator() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION_LIST, requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, url);
			if (currentRevision != headRevision) {
				urlGenerator.addParameter(RequestParameters.CREV,
						Long.toString(currentRevision));
			}
			if (state.getRequest().getParameter(
					RequestParameters.HIDE_POLARION_COMMIT) != null) {
				urlGenerator
						.addParameter(RequestParameters.HIDE_POLARION_COMMIT);
			}
			return urlGenerator;
		}

		protected void setNext(long revision) {
			this.start = revision;
			UrlGenerator urlGenerator = this.getUrlGenerator();
			urlGenerator.addParameter(RequestParameters.START_REVISION,
					Long.toString(revision));
			this.next = urlGenerator.getUrl();
		}

		protected void setPrevious(long revision) {
			this.end = revision;
			UrlGenerator urlGenerator = this.getUrlGenerator();
			urlGenerator.addParameter(RequestParameters.END_REVISION,
					Long.toString(revision));
			this.previous = urlGenerator.getUrl();
		}

		public String getNext() {
			return this.next;
		}

		public String getPrevious() {
			return this.previous;
		}

		public String getUrl() {
			return this.getUrlGenerator().getUrl();
		}

		public String getRange() {
			UrlGenerator urlGenerator = this.getUrlGenerator();
			urlGenerator.addParameter(RequestParameters.START_REVISION,
					Long.toString(this.start));
			urlGenerator.addParameter(RequestParameters.END_REVISION,
					Long.toString(this.end));
			return urlGenerator.getUrl();
		}

		public String getHidePolarionCommitUrl() throws ConfigurationException {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION_LIST, requestHandler.getLocation());
			Enumeration names = state.getRequest().getParameterNames();
			while (names.hasMoreElements()) {
				String paramName = (String) names.nextElement();
				if (!RequestParameters.HIDE_POLARION_COMMIT.equals(paramName)
						&& isAllowedHidePolarionCommit()) {
					urlGenerator.addParameter(paramName, state.getRequest()
							.getParameter(paramName));
				}
			}
			if (state.getRequest().getParameter(
					RequestParameters.HIDE_POLARION_COMMIT) == null) {
				urlGenerator
						.addParameter(RequestParameters.HIDE_POLARION_COMMIT);
			}
			return urlGenerator.getUrl();
		}
	}

	public RevisionListBean() {
		startRevision = -2;
		endRevision = -2;
		revisionCount = 0;
		isReverseOrder = false;
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		this.currentRevision = this.headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.currentRevision = this.requestHandler.getCurrentRevision();
		}
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.currentRevision);
		}

		this.setRevisionsStatus();
		this.info = dataProvider.getInfo(this.url, this.currentRevision);
		if (revisionCount == 0) {
			revisionCount = ConfigurationProvider.getInstance(
					dataProvider.getId()).getVersionsCount();
		}

		this.readRevisions(dataProvider, this.startRevision, this.endRevision);

		if (this.isReverseOrder) {
			Collections.reverse(this.revisions);
		}
		this.setNextDisabled(dataProvider);
		this.setPreviousDisabled(dataProvider);
		return true;
	}

	protected void readRevisions(IDataProvider dataProvider, long start,
			long end) throws SVNWebClientException {
		List pageRevisions = dataProvider.getRevisions(this.url, start, end,
				this.revisionCount);
		if (this.isAllowedHidePolarionCommit()
				&& this.isHidePolarionCommit == true) {
			long lastReadRevision = this.filterComments(pageRevisions);
			if (this.show_mode == RevisionListBean.SHOW_NEXT_MODE
					|| this.show_mode == RevisionListBean.SHOW_PREVIOUS_MODE) {
				if (this.revisions.size() == this.revisionCount) {
					return;
				} else {
					if (this.show_mode == RevisionListBean.SHOW_NEXT_MODE) {
						this.setNextDisabled(dataProvider, lastReadRevision);
						if (this.isNextDisabled) {
							return;
						} else {
							this.readRevisions(dataProvider,
									this.buttonUrls.getNextStartRevision(),
									(long) 0);
						}
					} else {
						this.setPreviousDisabled(dataProvider, lastReadRevision);
						if (this.isPreviousDisabled) {
							return;
						} else {
							this.readRevisions(dataProvider,
									this.buttonUrls.getPreviousEndRevision(),
									this.currentRevision);
						}
					}
				}
			}
		} else {
			this.revisions = pageRevisions;
		}
	}

	protected long filterComments(List pageRevisions) {
		Iterator it = pageRevisions.iterator();
		long lastRevision = 0;
		while (it.hasNext()) {
			if (this.revisionCount == 0 || this.revisionCount == -1
					|| this.revisions.size() < this.revisionCount) {
				DataRevision rev = (DataRevision) it.next();
				lastRevision = rev.getRevision();
				String comment = rev.getComment();
				if (this.isPolarionComment(comment)) {
					it.remove();
				} else {
					this.revisions.add(rev);
				}
			} else {
				break;
			}
		}
		return lastRevision;
	}

	protected boolean isPolarionComment(String comment) {
		Pattern pattern = Pattern.compile("Polarion commit");
		Matcher matcher = pattern.matcher(comment);
		return matcher.find();
	}

	protected void setRevisionsStatus() {
		this.isHidePolarionCommit = state.getRequest().getParameter(
                RequestParameters.HIDE_POLARION_COMMIT) != null;
		if (this.state.getRequest().getParameter(
				RequestParameters.START_REVISION) != null) {
			this.startRevision = Long.parseLong(this.state.getRequest()
					.getParameter(RequestParameters.START_REVISION));
		}
		if (this.state.getRequest()
				.getParameter(RequestParameters.END_REVISION) != null) {
			this.endRevision = Long.parseLong(this.state.getRequest()
					.getParameter(RequestParameters.END_REVISION));
		}
		this.revisionCount = this.requestHandler.getRevisionCount();

		if (this.startRevision != -2 && this.endRevision == -2) {
			// next
			this.endRevision = 0;
			this.show_mode = RevisionListBean.SHOW_NEXT_MODE;
		} else if (this.startRevision != -2 && this.endRevision != -2) {
			// range
			revisionCount = -1;
			if (startRevision < endRevision) {
				isReverseOrder = true;
			}
			this.show_mode = RevisionListBean.SHOW_OTHER_MODE;
		} else if (this.startRevision == -2 && this.endRevision != -2) {
			// previous
			this.startRevision = this.endRevision;
			this.endRevision = this.currentRevision;
			this.isReverseOrder = true;
			this.show_mode = RevisionListBean.SHOW_PREVIOUS_MODE;
		} else {
			startRevision = this.currentRevision;
			endRevision = 0;
			revisionCount = -1;
			this.show_mode = RevisionListBean.SHOW_OTHER_MODE;
		}
	}

	protected long getSibblingRevision(long startRevision, long endREvision,
			IDataProvider dataProvider) throws SVNWebClientException {
		try {
			long count = 2;
			List revList = dataProvider.getRevisions(this.url, startRevision,
					endREvision, count);
			if (revList.size() == 1) {
				return ((DataRevision) revList.get(0)).getRevision();
			} else {
				return ((DataRevision) revList.get(1)).getRevision();
			}
		} catch (IncorrectParameterException ie) {
			// it was copy operation
			return startRevision;
		} catch (DataProviderException de) {
			throw new SVNWebClientException(de);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public RevisionList getRevisionList() throws ConfigurationException {
		RevisionList revisionList = new RevisionList(this.revisions,
				this.requestHandler, this.currentRevision, this.url, this.info,
				ConfigurationProvider.getInstance(dataProvider.getId())
						.getRevisionDecorator());
		revisionList.setState(this.state);
		return revisionList;
	}

	public Navigation getNavigation() throws ConfigurationException {
		return new Navigation(dataProvider.getId(), this.url,
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(),
				!this.info.isDirectory());
	}

	public List getActions() {
		List ret = new ArrayList();
		UrlGenerator urlGenerator;
		if (this.info.isDirectory()) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.DIRECTORY_COMPARE, this.requestHandler.getLocation());
		} else {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_COMPARE, this.requestHandler.getLocation());
		}
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		ret.add(new Button("javascript:compareRevisions('"
				+ urlGenerator.getUrl() + "')", Images.COMPARE, "Compare"));

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.STATS_ITEM,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));

		Long pegRevision = -1L;
		if (this.requestHandler.getCurrentRevision() != -1) {
			pegRevision = this.requestHandler.getCurrentRevision();
		} else {
			pegRevision = this.headRevision;
		}
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(this.url));
		urlGenerator.addParameter(RequestParameters.PEGREV,
				Long.toString(pegRevision));
		ret.add(new Button(urlGenerator.getUrl(), Images.STATISTICS,
				"Statistics"));
		return ret;
	}

	public String getEndRevision() {
		return Long.toString(((DataRevision) this.revisions.get(this.revisions
				.size() - 1)).getRevision());
	}

	public String getStartRevision() {
		return Long.toString(((DataRevision) this.revisions.get(0))
				.getRevision());
	}

	protected void setNextDisabled(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.setNextDisabled(dataProvider, -2);
	}

	protected void setPreviousDisabled(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.setPreviousDisabled(dataProvider, -2);
	}

	protected void setNextDisabled(IDataProvider dataProvider, long start)
			throws SVNWebClientException {
		long nextRevision = 0;
		if (start == -2) {
			start = Long.parseLong(this.getEndRevision());
		}
		if (start == 0) {
			this.isNextDisabled = true;
		} else {
			nextRevision = this.getSibblingRevision(start, (long) 0,
					dataProvider);
            this.isNextDisabled = nextRevision == start;
		}
		this.buttonUrls.setNext(nextRevision);
	}

	protected void setPreviousDisabled(IDataProvider dataProvider, long start)
			throws SVNWebClientException {
		long previousRevision = 0;
		if (start == -2) {
			start = Long.parseLong(this.getStartRevision());
		}
		if (start == this.currentRevision) {
			this.isPreviousDisabled = true;
		} else {
			previousRevision = this.getSibblingRevision(start,
					this.currentRevision, dataProvider);
            this.isPreviousDisabled = previousRevision == start;
		}
		this.buttonUrls.setPrevious(previousRevision);
	}

	public boolean isNextDisabled() {
		return this.isNextDisabled;
	}

	public boolean isPreviousDisabled() {
		return this.isPreviousDisabled;
	}

	public String getUrl() {
		return this.url;
	}

	public String getRangeStartRevision() {
		return (this.state.getRequest().getParameter(
				RequestParameters.START_REVISION) == null ? "" : this.state
				.getRequest().getParameter(RequestParameters.START_REVISION));
	}

	public String getRangeEndRevision() {
		return (this.state.getRequest().getParameter(
				RequestParameters.END_REVISION) == null ? "" : this.state
				.getRequest().getParameter(RequestParameters.END_REVISION));
	}

	public ButtonUrls getButtonUrl() {
		return this.buttonUrls;
	}

	public String getSelectUrl() {
		return Links.CHANGE_REVISION_MODE + "?"
				+ this.state.getRequest().getQueryString();
	}

	public boolean isHidePolarionCommit() {
		return this.isHidePolarionCommit;
	}

	public boolean isAllowedHidePolarionCommit() throws ConfigurationException {
		return (ConfigurationProvider.getInstance(dataProvider.getId())
				.isEmbedded() && ConfigurationProvider.getInstance(
				dataProvider.getId()).isHidePolarionCommit());
	}
}
