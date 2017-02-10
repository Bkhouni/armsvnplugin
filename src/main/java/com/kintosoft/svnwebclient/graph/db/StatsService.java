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

package com.kintosoft.svnwebclient.graph.db;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RemoteProxy
public class StatsService {
	private static Logger log = LoggerFactory.getLogger(StatsService.class);

	private static ActiveObjects ao;

	public StatsService(ActiveObjects ao){
		StatsService.ao = ao;
	}

	@RemoteMethod
	public Map<String, Map<Date, Integer>> getHistorySizeByUser(long repoId,
			String item, long pegRevision, boolean isDirectory)
			throws Exception {

		CommitGraphService.checkPrivileges(repoId);

		try {

			DBGraph dbGraph = new DBGraph(ao);
			Map<String, Map<Date, Integer>> sizes = dbGraph
					.getHistorySizeGroupByUserDay(item, pegRevision, repoId,
							isDirectory);

			return sizes;
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	private String toCVS(Map<String, Map<String, Integer>> sizes) {
		StringBuffer sb = new StringBuffer("user,date,size");
		for (Map.Entry<String, Map<String, Integer>> userSize : sizes
				.entrySet()) {
			String user = userSize.getKey();
			for (Map.Entry<String, Integer> userDay : userSize.getValue()
					.entrySet()) {
				sb.append("\n");
				sb.append(user).append(",").append(userDay.getKey())
						.append(",").append(userDay.getValue());
			}
		}
		return sb.toString();
	}

	@RemoteMethod
	public Map<String, Map<Date, Integer>> getHistorySizeForUser(long repoId,
			String username) throws Exception {

		CommitGraphService cgs = new CommitGraphService(ao);
		CommitGraphService.checkPrivileges(repoId);

		try {
			Map<String, Map<Date, Integer>> sizes = new HashMap<String, Map<Date, Integer>>();
			DBGraph dbGRaph = new DBGraph(ao);
			sizes.put(username,
					dbGRaph.getHistorySizeForUserDay(repoId, username));
			return sizes;
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	@RemoteMethod
	public Map<String, Map<Date, Integer>> getHistorySizeForJIRAFilter(
			long filterId) throws Exception {
		// implicit security through the query
		try {
			DBGraph dbGraph = new DBGraph(ao);
			Map<String, Map<Date, Integer>> sizes = dbGraph
					.getHistorySizeForJIRAFilter(filterId);

			return sizes;
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	public static List<SVNLogEntry> getUserCommitsForJIRAFilter(String author,
			long filterId) throws Exception {

		// implicit security through the query
		try {
			DBGraph dbGraph = new DBGraph(ao);
			return dbGraph.getUserCommitsForJIRAFilter(author, filterId);
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	public static List<SVNLogEntry> getDateCommitsForJIRAFilter(String date,
			long filterId) throws Exception {

		// implicit security through the query
		try {
			DBGraph dbGraph = new DBGraph(ao);
			return dbGraph.getDateCommitsForJIRAFilter(date, filterId);
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}
}
