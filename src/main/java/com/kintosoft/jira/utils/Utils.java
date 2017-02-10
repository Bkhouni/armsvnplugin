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

package com.kintosoft.jira.utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.user.ApplicationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class Utils {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);

	public static String getFlterName(long filterId) {

		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext()
				.getLoggedInUser();

		SearchRequestManager searchRequestManager = ComponentAccessor
				.getComponent(SearchRequestManager.class);

		SearchRequest searchRequest = searchRequestManager
				.getSearchRequestById(user, filterId);

		return searchRequest.getName();

	}

	public static String getFilerName(String filterId) {
		try {
			return getFlterName(Long.parseLong(filterId));
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return "<Unknown filter: " + filterId + ">";
		}
	}

	public static byte[] encryptDecryptLicense(byte[] licBytes, byte[] key) {
		byte[] output = new byte[licBytes.length];
		for (int i = 0; i < licBytes.length; i++) {
			output[i] = (byte) (licBytes[i] ^ key[i]);
		}
		return output;
	}

	public static byte[] generateKey(int size) {
		SecureRandom rng = new SecureRandom();
		byte[] strongBytes = new byte[size];
		rng.nextBytes(strongBytes);
		return strongBytes;
	}
}
