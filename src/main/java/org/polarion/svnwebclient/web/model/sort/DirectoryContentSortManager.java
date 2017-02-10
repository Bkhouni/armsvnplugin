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

package org.polarion.svnwebclient.web.model.sort;

import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.http.Cookie;
import java.util.Comparator;
import java.util.Date;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class DirectoryContentSortManager {
	protected static final String COOKIE_FIELD_NAME = "DirectoryContentSortField";
	protected static final String COOKIE_SORT_ORDER = "DirectoryContentSortOrder";

	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";

	public static final String FIELD_NAME = "name";
	public static final String FIELD_REVISION = "revision";
	public static final String FIELD_DATE = "date";
	public static final String FIELD_AUTHOR = "author";
	public static final String FIELD_SIZE = "size";

	protected State state;
	protected AbstractRequestHandler requestHandler;
	protected String sortField;
	protected boolean sortAscending;

	public DirectoryContentSortManager(State state,
			AbstractRequestHandler requestHandler) {
		this.state = state;
		this.requestHandler = requestHandler;

		this.sortField = this.requestHandler.getSortField();
		if (this.sortField == null) {
			String sortOrder = null;
			Cookie[] cookies = this.state.getRequest().getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (COOKIE_FIELD_NAME.equals(cookies[i].getName())) {
						this.sortField = cookies[i].getValue();
					} else if (COOKIE_SORT_ORDER.equals(cookies[i].getName())) {
						sortOrder = cookies[i].getValue();
					}
				}
			}

			if (this.sortField == null) {
				this.sortField = DirectoryContentSortManager.FIELD_NAME;
			}
			this.initSortOrder(sortOrder);
		} else {
			this.initSortOrder(this.requestHandler.getSortOrder());
		}

		this.saveSettingsInCookies();
	}

	protected void initSortOrder(String sortOrder) {
		if (DirectoryContentSortManager.ORDER_ASC.equals(sortOrder)) {
			this.sortAscending = true;
		} else this.sortAscending = !DirectoryContentSortManager.ORDER_DESC.equals(sortOrder);
	}

	protected void saveSettingsInCookies() {
		// set expiration date on 2 years
		int expiry = 2 * 365 * 24 * 60 * 60;

		Cookie fieldCookie = new Cookie(
				DirectoryContentSortManager.COOKIE_FIELD_NAME, this.sortField);
		fieldCookie.setMaxAge(expiry);
		this.state.getResponse().addCookie(fieldCookie);

		Cookie orderCookie;
		if (this.sortAscending) {
			orderCookie = new Cookie(
					DirectoryContentSortManager.COOKIE_SORT_ORDER,
					DirectoryContentSortManager.ORDER_ASC);
		} else {
			orderCookie = new Cookie(
					DirectoryContentSortManager.COOKIE_SORT_ORDER,
					DirectoryContentSortManager.ORDER_DESC);
		}
		orderCookie.setMaxAge(expiry);
		this.state.getResponse().addCookie(orderCookie);
	}

	public boolean hasSortIcon(String field) {
		return this.sortField.equals(field);
	}

	public String getSortIcon(String field) {
		if (this.sortField.equals(field)) {
			if (this.sortAscending) {
				return Images.ASC;
			} else {
				return Images.DESC;
			}
		} else {
			return null;
		}
	}

	public String getSortUrl(String field) {
		String sortOrder;
		if (this.sortAscending) {
			sortOrder = DirectoryContentSortManager.ORDER_DESC;
		} else {
			sortOrder = DirectoryContentSortManager.ORDER_ASC;
		}
		StringBuffer url = this.state.getRequest().getRequestURL();
		String queryString = this.state.getRequest().getQueryString();
		boolean firstParameter = true;
		if (queryString != null) {
			String[] parameters = queryString.split("&");
			for (int i = 0; i < parameters.length; i++) {
				String[] data = parameters[i].split("=");
				String parameterName = data[0];
				if (!RequestParameters.SORT_FIELD.equals(parameterName)
						&& !RequestParameters.SORT_ORDER.equals(parameterName)) {
					if (firstParameter) {
						firstParameter = false;
						url.append("?");
					} else {
						url.append("&");
					}
					url.append(parameterName);
					if (data.length > 1) {
						url.append("=");
						url.append(data[1]);
					}
				}
			}
		}
		if (firstParameter) {
			firstParameter = false;
			url.append("?");
		} else {
			url.append("&");
		}
		url.append(RequestParameters.SORT_FIELD);
		url.append("=");
		url.append(field);
		url.append("&");
		url.append(RequestParameters.SORT_ORDER);
		url.append("=");
		url.append(sortOrder);
		return url.toString();
	}

	public Comparator getComparator() {
		Comparator ret = null;
		if (DirectoryContentSortManager.FIELD_NAME.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					int ret;
					if (((DataRepositoryElement) o1).isDirectory() == ((DataRepositoryElement) o2)
							.isDirectory()) {
						ret = ((DataRepositoryElement) o1)
								.getName()
								.toLowerCase()
								.compareTo(
										((DataRepositoryElement) o2).getName()
												.toLowerCase());
						if (!DirectoryContentSortManager.this.sortAscending) {
							ret = -ret;
						}
					} else {
						ret = ((DataRepositoryElement) o1).isDirectory() ? -1
								: 1;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_REVISION
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					int ret;
					if (((DataRepositoryElement) o1).getRevision() == ((DataRepositoryElement) o2)
							.getRevision()) {
						ret = 0;
					} else {
						ret = (((DataRepositoryElement) o1).getRevision() > ((DataRepositoryElement) o2)
								.getRevision()) ? 1 : -1;
					}
					if (!DirectoryContentSortManager.this.sortAscending) {
						ret = -ret;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_DATE
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}

					Date d1 = ((DataRepositoryElement) o1).getDate();
					Date d2 = ((DataRepositoryElement) o2).getDate();

					if (d1 == null || d2 == null) {
						return 0;
					}

					int ret = d1.compareTo(d2);
					if (!DirectoryContentSortManager.this.sortAscending) {
						ret = -ret;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_AUTHOR
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}

					DataRepositoryElement de1 = (DataRepositoryElement) o1;
					DataRepositoryElement de2 = (DataRepositoryElement) o2;

					String author1 = de1.getAuthor();
					String author2 = de2.getAuthor();

					int ret = 0;
					if (author1 == null) {
						ret = 1;
					} else if (author2 == null) {
						ret = -1;
					} else {
						ret = author1.compareToIgnoreCase(author2);
						if (!DirectoryContentSortManager.this.sortAscending) {
							ret = -ret;
						}
					}

					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_SIZE
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					if (((DataRepositoryElement) o1).getSize() == ((DataRepositoryElement) o2)
							.getSize()) {
						return 0;
					}
					boolean greater = ((DataRepositoryElement) o1).getSize() > ((DataRepositoryElement) o2)
							.getSize();
					int ret;
					if (DirectoryContentSortManager.this.sortAscending) {
						ret = (greater) ? 1 : -1;
					} else {
						ret = (greater) ? -1 : 1;
					}
					return ret;
				}
			};
		}

		return ret;
	}
}
