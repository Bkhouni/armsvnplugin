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

package com.kintosoft.jira.plugin.ext.subversion.action;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.scheduler.SchedulerService;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManagerImpl;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.jira.plugin.ext.subversion.revisions.scheduling.ScheduleMgr;
import com.kintosoft.jira.utils.Keys;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Manage 1 or more repositories
 */


public class ViewSubversionRepositoriesAction extends SubversionActionSupport {

	private static final Logger log = LogManager.getLogger("atlassian.plugin");
	private final SchedulerService schedulerService;

	PluginConnectionPool pcp;


	public ViewSubversionRepositoriesAction(ActiveObjects ao, ALMMultipleSubversionRepositoryManager multipleRepoManager) throws IOException, SQLException {
		super(multipleRepoManager, ao);
		pcp = new PluginConnectionPool(ao);


		this.schedulerService = ComponentAccessor.getComponent(SchedulerService.class);
	}

	public List<SubversionManager> getRepositories() {
		List<SubversionManager> subversionManagers = new ArrayList<SubversionManager>(
				getMultipleRepoManager().getRepositoryList());

		Collections.sort(subversionManagers,
				new Comparator<SubversionManager>() {
					public int compare(SubversionManager left,
							SubversionManager right) {
						return left.getDisplayName().compareToIgnoreCase(
								right.getDisplayName());
					}
				});

		return subversionManagers;
	}

	public String getIndexerError() {
		if(getMultipleRepoManager().getIndexer() != null)
			return getMultipleRepoManager().getIndexer().error;
		else
			return "error";

	}

	public long getLatestCachedRevision(long repoId) {
		try {
			return getMultipleRepoManager().getIndexer()
					.getLatestCachedRevision(repoId);
		} catch (SQLException e) {
			return -1;
		}
	}

	public String getPoolUrl() {
		return "Pool URL Here";
	}

	public String getIndexPath() {
		return "Index Path Here";
	}

	public String getInterval() {
		String interval;
		try {
			int seconds = ScheduleMgr.getInstance(getMultipleRepoManager(), schedulerService).getIntervalFromDB();
			interval = Integer.toString(seconds);
		} catch (SQLException e) {
			interval = e.getMessage();
		}
		return interval;
	}

	public String getLatestFire() {
		Date d = null;
		try {
			d = ScheduleMgr.getInstance(getMultipleRepoManager(), schedulerService).getLatestFire();
		} catch (SchedulerException e) {
			log.warn(e.getMessage());
			return e.getMessage();
		}
		if (d == null) {
			return "No yet fired";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(d);
	}

	public String doAgree() throws SQLException {
		PluginConnectionPool.setProperty(Keys.db.accepted, "true");




		PluginConnectionPool.setActiveObjects(ao);
		try {
			PluginConnectionPool.createSchema();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return getRedirect("ALMViewSubversionRepositories.jspa");
	}

	public boolean getAgree() throws SQLException {

		return Boolean.parseBoolean(PluginConnectionPool
				.getProperty(Keys.db.accepted));
	}

	public int getPoolSize() {
		try {
			return PluginConnectionPool.getDBConnectionPoolSize();
		} catch (SQLException e) {
			log.error(e.getMessage());
			return -1;
		}
	}

	public boolean getShareConnections() {
		try {
			return PluginConnectionPool.getShareConnections();
		} catch (SQLException e) {
			log.error(e.getMessage());
			return false;
		}

	}

	public boolean getCompactOnClose() {
		return PluginConnectionPool.getCompactOnClose();
	}

	public boolean getRequireTrackerSession() {
		try {
			return PluginConnectionPool
					.getRequireTrackerSession();
		} catch (SQLException e) {
			log.error(e.getMessage());
			return false;
		}

	}

	public int getSVNConnectionTimeout() {
		return PluginConnectionPool.getSVNConnectionTimeout();
	}

	public int getSVNReadTimeout() {
		return PluginConnectionPool.getSVNReadTimeout();
	}

	public int getMaxIndexingThreads() {
		try {
			return PluginConnectionPool.getMaxIndexingThreads();
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		return -1;
	}
}
