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

package com.kintosoft.svnwebclient.indexing;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import com.kintosoft.svnwebclient.graph.entities.ao.*;
import com.kintosoft.svnwebclient.graph.entities.managers.ActionManager;
import com.kintosoft.svnwebclient.graph.entities.managers.CommentManager;
import com.kintosoft.svnwebclient.graph.entities.managers.KeyManager;
import com.kintosoft.svnwebclient.graph.entities.managers.RevisionManager;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;


public class RevisionIndexer {

	private final static Logger log = LoggerFactory
			.getLogger(RevisionIndexer.class);
	private static final Long NOT_INDEXED = -1L;

	private final ALMMultipleSubversionRepositoryManager multipleSubversionRepositoryManager;
	private final VersionManager versionManager;
	private final IssueManager issueManager;
	private final PermissionManager permissionManager;
	private final ChangeHistoryManager changeHistoryManager;
	public String error;


	private final ActiveObjects ao;

	private final RevisionManager revisionManager;
	private final KeyManager keyManager;
	private final CommentManager commentManager;
	private final ActionManager actionManager;


	public RevisionIndexer(
			ALMMultipleSubversionRepositoryManager almMultipleSubversionRepositoryManager,
			VersionManager versionManager, IssueManager issueManager,
			PermissionManager permissionManager,
			ChangeHistoryManager changeHistoryManager, ActiveObjects ao) {
		this.multipleSubversionRepositoryManager = almMultipleSubversionRepositoryManager;
		this.versionManager = versionManager;
		this.issueManager = issueManager;
		this.permissionManager = permissionManager;
		this.changeHistoryManager = changeHistoryManager;
		this.ao = ao;

		revisionManager = new RevisionManager(ao);
		keyManager = new KeyManager(ao);
		commentManager = new CommentManager(ao);
		actionManager = new ActionManager(ao);
	}

	public void addRepository(SubversionManager subversionInstance) {
		try {
			log.debug("adding the Subversionmanager instance to the Revision indexer ~ update the index");
			updateIndex();
		} catch (Exception e) {
			throw new InfrastructureException("Could not index repository", e);
		}
	}

	synchronized public void updateIndex() throws Exception {

		Collection<SubversionManager> repositories = multipleSubversionRepositoryManager
				.getRepositoryList();

		// temp log comment
		if (log.isDebugEnabled())
			log.debug("Number of repositories: " + repositories.size());

		final int MAX_THREADS = PluginConnectionPool.getMaxIndexingThreads();
		log.debug("Max parallel indexing porcesses:" + MAX_THREADS);
		int liveThreads = 0;
		for (SubversionManager subversionManager : repositories) {
			long repoId = subversionManager.getId();
			log.debug("Analyzing repo: [" + repoId + "] ...");
			try {

				if (!subversionManager.isActive()) {
					subversionManager.activate();
					if (!subversionManager.isActive()) {
						log.debug("...the SubversionManager has NOT been activated (id="
								+ subversionManager.getId() + ").");
						continue;
					} else {
						log.debug("...the SubversionManager has been activated (id="
								+ subversionManager.getId() + ").");
					}
				}

				if (subversionManager.isBeingIndexed()) {
					liveThreads++;
					log.debug("["
							+ repoId
							+ "] ...discarded becuase it is being indexed. Number of threads:"
							+ liveThreads);
					continue;
				}

				if (liveThreads >= MAX_THREADS) {
					log.debug("Aborting  the indexing process because achieved the max number of allowed threads.");
					break;
				}

				long latestCachedRevision = getLatestCachedRevision(repoId);

				long head = subversionManager.getLatestRevision();
				if (head == -1 || head == latestCachedRevision) {
					log.debug("["
							+ repoId
							+ "] ...discarded becuase it is up to date. Number of threads:"
							+ liveThreads);
					continue;
				}

				subversionManager.updateIndex(latestCachedRevision);
				liveThreads++;
				log.debug("[" + repoId
						+ "] ...indexing started. Number of threads:"
						+ liveThreads);
			} catch (Exception e) {
				log.warn(
						"Unable to index repository '"
								+ subversionManager.getDisplayName() + "'", e);
			}
		}
	}

	private long updateLastRevisionIndexed(long repoId) throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("Updating last revision indexed.");
		}

		// find all log entries that have already been indexed for the specified
		// repository
		// (i.e. all logs that have been associated with issues in JIRA)
		long latestIndexedRevision = getLatestCachedRevision(repoId);

		return latestIndexedRevision;
	}

	public void removeEntries(SubversionManager subversionInstance)
			throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("Deleteing revisions for : "
					+ subversionInstance.getRoot());
		}

		long repoId = subversionInstance.getId();
		SWCUtils.setAO(ao, multipleSubversionRepositoryManager);
		SWCUtils.deleteRepository(repoId);

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// // Database methods

	//VALIANTYS - DONE
	public long getLatestCachedRevision(long repoId) throws SQLException {
		long res = 0L;
		List<Revision> revisions = revisionManager.getRevisionsByRepo((int)repoId);
		if(revisions != null && revisions.size()>0){
			res = revisions.get(0).getRevision();
		}
		if(res == 0){
			long countRev = revisionManager.getRevisionsByRepoCount((int)repoId);
			if(countRev == 0L){
				res = NOT_INDEXED;
			}
		}

		/*String sql = "select max(REVISION) from REVISIONS where REPOID=?";
		String sql2 = "select count(*) from REVISIONS where REPOID=?";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnetion();
			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				res = rs.getLong(1);
			}
			rs.close();
			if (res == 0) {
				ps.close();
				ps = conn.prepareStatement(sql2);
				ps.setLong(1, repoId);
				rs = ps.executeQuery();
				if (rs.next()) {
					if (rs.getLong(1) == 0L) {
						res = NOT_INDEXED;
					}
				}
				rs.close();
			}
			return res;
		} finally {
			PluginConnectionPool.closeStatement(ps);
			PluginConnectionPool.closeConnection(conn);
		}*/

		return res;
	}

	//VALIANTYS - DONE
	public List<SVNLogEntry> getLogEntriesByRepository(
			Collection<String> issues, int startIndex, int pageSize,
			boolean ascending) throws SQLException {

		/*Connection conn = null;
		PreparedStatement ps = null;*/
//		try {

			/*String sqlBase = "select distinct repoId, revision from keys where project=? and issue=?";

			String sql = sqlBase;
			for (int i = 1; i < issues.size(); i++) {
				sql += " UNION " + sqlBase;
			}

			if (issues.size() > 1) {
				sql = "select * from (" + sql + ")";
			}

			sql += " order by revision " + (ascending ? "asc" : "desc");
			sql += " limit ? offset ?";

			conn = getConnetion();
			ps = conn.prepareStatement(sql);

			int count = 0;
			for (String issue : issues) {
				if (log.isDebugEnabled())
					log.debug("Retrieving revisions for : " + issue);
				String projectKey = IssueKey.from(issue).getProjectKey();
				long issueCounter = IssueKey.from(issue).getIssueNumber();
				if (projectKey == null || issueCounter == -1) {
					log.warn("Invalid issue key:" + issue);
					return null;
				}

				ps.setString(++count, projectKey);
				ps.setLong(++count, issueCounter);
			}

			ps.setLong(++count, pageSize);
			ps.setLong(++count, startIndex);
*/
			List<Key> result = keyManager.getKeysByIssues(issues, startIndex, pageSize, ascending);
//			List<SVNLogEntry> logEntries = populateLogEntriesinTrans(conn, rs);

			List<SVNLogEntry> logEntries = populateLogEntriesinTrans(result);
//			rs.close();
			return logEntries;
		/*} finally {
            if (ps != null) {
                ps.close();
            }
            PluginConnectionPool.closeConnection(conn);
        }*/
	}

	private List<SVNLogEntry> populateLogEntriesinTrans(List<Key> rs) throws SQLException {
		List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
		for(Key key: rs) {

			long repoId = key.getRepository().getID();
			long revision = key.getRevision().getRevision();

			Revision revisionObject = key.getRevision();


			int revisionId = revisionObject.getID();

			Map<String, Object> revisionInfo = getRevisionInfoInTrans(repoId, revision);

			String message = getCommentForRevisionInTrans(repoId, revisionId);

			String author = (String) revisionInfo.get("author");
			Timestamp ts = (Timestamp) revisionInfo.get("timestamp");

			Date date = new Date(ts.getTime());


			List<Action> actions = actionManager.getActionItemByRepoByRevision((int)repoId, revisionId);
			Map<String, SVNLogEntryPath> changedPaths = getLogEntryPathsInTrans(repoId, revisionId, ao);

			SVNLogEntry entry = new SVNLogEntry(repoId, changedPaths, revision,
					author, date, message);
			logEntries.add(entry);
		}

		Collections.sort(logEntries, new Comparator<SVNLogEntry>() {

			@Override
			public int compare(SVNLogEntry a, SVNLogEntry b) {
				if (b.getDate() == null) {
					return 1;
				}
				if (a.getDate() == null) {
					return -1;
				}

				return (int) Math.signum(b.getDate().getTime()
						- a.getDate().getTime());

			}
		});

		return logEntries;
	}

	//VALIANTYS - DONE
	private Map<String, Object> getRevisionInfoInTrans(long repoId, long revision) throws SQLException {
		Map<String, Object> revisionInfo = new HashMap<String, Object>();

		List<Revision> rs = revisionManager.getRevisionsByRepoByRevision((int)repoId, (int)revision);

		if(rs != null) {
			for (Revision rev : rs) {
				revisionInfo.put("author", rev.getAuthor());
				Timestamp ts = rev.getRTimestamp();
				revisionInfo.put("timestamp", ts);

				Date date = ts == null ? null : new Date(ts.getTime());
				revisionInfo.put("date", date);
			}
		}
		else{
			log.warn("No revision information found for repositoryId="
					+ repoId + " and revision=" + revision);
		}

		/*String sql = "select * from revisions where repoid=? and revision=?";
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);
			ps.setLong(2, revision);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				revisionInfo.put("author", rs.getString("author"));
				Timestamp ts = rs.getRTimestamp("timestamp");
				revisionInfo.put("timestamp", ts);
				Date date = ts == null ? null : new Date(ts.getTime());
				revisionInfo.put("date", date);
			} else {
				log.warn("No revision information found for repositoryId="
						+ repoId + " and revision=" + revision);
			}
			rs.close();
			return revisionInfo;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/

		return revisionInfo;
	}

	//VALIANTYS - DONE
	private String getCommentForRevisionInTrans(long repoId, long revision) throws SQLException {

		List<Comment> rs = commentManager.getCommentByRepoByRevision(repoId, revision);
		if(rs == null || rs.size() == 0){
			log.warn("No revision information found for repositoryId="
					+ repoId + " and revision=" + revision);
			return "";
		}
		return rs.get(0).getComment();
		/*String sql = "select comment from comments where repoid=? and revision=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);
			ps.setLong(2, revision);
			rs = ps.executeQuery();
			if (!rs.next()) {
				log.warn("No revision information found for repositoryId="
						+ repoId + " and revision=" + revision);
				return "";
			}
			return rs.getString("comment");
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}*/
	}


	//VALIANTYS - DONE
	public static Map<String, SVNLogEntryPath> getLogEntryPathsInTrans(
			long repoId, long revision, ActiveObjects ao) throws SQLException {
		/*String sql = "select A.action, i.path, i.name, a.revision from ACTIONS as A, ITEMS as I where a.itemid=i.id and a.repoId=? and a.revision=? order by i.path, i.name";
		PreparedStatement ps = null;*/
		Query query = Query.select().alias(Item.class, "i").alias(Action.class, "a")
				.join(Item.class,"a.ITEM_ID = i.ID")
				.where("a.REPO_ID = ? and a.REVISION_ID = ? ", repoId, revision);

		List<Action> actions = newArrayList(ao.find(Action.class, query));
		Map<String, SVNLogEntryPath> logEntryPaths = getLogEntryPathFromActions(actions);

		/*try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, repoId);
			ps.setLong(2, revision);
			ResultSet rs = ps.executeQuery();*/




			/*while (rs.next()) {
				char type = rs.getString("action").charAt(0);
				String path = rs.getString("path") + rs.getString("name");
				SVNLogEntryPath logEntrypath = new SVNLogEntryPath(path, type,
						null, -1);
				logEntryPaths.put(path, logEntrypath);
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		return logEntryPaths;

	}


	public static Map<String, SVNLogEntryPath> getLogEntryPathFromActions(List<Action> actions){
		Map<String, SVNLogEntryPath> logEntryPaths = new HashMap<String, SVNLogEntryPath>();
		for(Action action:actions){
			char type = action.getAction().charAt(0);
			String path = action.getItem().getPath() + action.getItem().getName();

			SVNLogEntryPath logEntrypath = new SVNLogEntryPath(path, type,
					null, -1);
			logEntryPaths.put(path, logEntrypath);

		}
		return logEntryPaths;
	}

	//VALIANTYS - DONE
	public List<SVNLogEntry> getLogEntriesByProject(String projectKey,
			ApplicationUser user, int startIndex, int pageSize)
			throws SQLException {

	    List<Key> result = keyManager.getKeysByProject(projectKey,user,startIndex,pageSize);
        List<SVNLogEntry> logEntries = populateLogEntriesinTrans(result);

		/*String sql = "select distinct repoId, revision from keys where project=? and HAS_PRIVILEGES_VIEW_VERSION_CONTROL(?,project,issue) order by revision desc";

		sql += " limit ? offset ?";

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnetion();
			ps = conn.prepareStatement(sql);
			ps.setString(1, projectKey);
			ps.setString(2, user.getName());

			ps.setLong(3, pageSize);
			ps.setLong(4, startIndex);

			ResultSet rs = ps.executeQuery();
			List<SVNLogEntry> logEntries = populateLogEntriesinTrans(conn, rs);
			rs.close();
			return logEntries;
		} finally {
			if (ps != null) {
				ps.close();
			}
			PluginConnectionPool.closeConnection(conn);
		}*/
        return logEntries;
	}

    //VALIANTYS - DONE
	public List<SVNLogEntry> getLogEntriesByVersion(Version version,
			ApplicationUser user, int startIndex, int pageSize)
			throws SQLException {

		// Find all isuses affected by and fixed by any of the versions:
		Collection<Issue> issues = new HashSet<Issue>();

		issues.addAll(versionManager.getIssuesWithFixVersion(version));
		issues.addAll(versionManager.getIssuesWithAffectsVersion(version));

		Set<String> permittedIssueKeys = new HashSet<String>();
		String issueKeysIn = "";
		for (Issue issue : issues) {
			String key = issue.getString("key");
			Issue theIssue = issueManager.getIssueObject(key);

			if (permissionManager.hasPermission(
					Permissions.BROWSE, theIssue, user)) {
				if (issueKeysIn.length() == 0) {
					issueKeysIn = key;
				} else {
					issueKeysIn += "," + key;
				}
				permittedIssueKeys.add(key);
			}
		}


		List<Key> result = keyManager.getKeysByIssueKey(issueKeysIn,startIndex,pageSize);
        List<SVNLogEntry> logEntries = populateLogEntriesinTrans(result);


		/*String sql;

		sql = "select  ROWNUM() as RN, repoId, revision from keys where ISSUEKEY(project,issue) in ('"
				+ issueKeysIn + "') order by revision desc";
		sql = "select * from (" + sql + ") where RN between ? and ?";

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnetion();
			ps = conn.prepareStatement(sql);

			ps.setLong(1, startIndex + 1);
			ps.setLong(2, startIndex + 1 + pageSize);

			ResultSet rs = ps.executeQuery();
			List<SVNLogEntry> logEntries = populateLogEntriesinTrans(conn, rs);
			rs.close();
			return logEntries;
		} finally {
			if (ps != null) {
				ps.close();
			}
			PluginConnectionPool.closeConnection(conn);
		}*/

        return logEntries;
	}

	/*public Connection getConnetion() throws SQLException {
		return PluginConnectionPool.getConnection();
	}*/

	public boolean isBeingIndexed(long repoId) {
		Collection<SubversionManager> repositories = multipleSubversionRepositoryManager
				.getRepositoryList();

		for (SubversionManager manager : repositories) {
			if (manager.getId() == repoId) {
				return manager.isBeingIndexed();
			}
		}
		return false;
	}

	public int getCurrentActiveThreads() {
		Collection<SubversionManager> repositories = multipleSubversionRepositoryManager
				.getRepositoryList();
		int count = 0;
		for (SubversionManager manager : repositories) {
			if (manager.isBeingIndexed()) {
				count++;
			}
		}
		return count;
	}

	synchronized public void terminate(long repoId) {
		SubversionManager manager = multipleSubversionRepositoryManager
				.getRepository(repoId);
		if (manager == null) {
			log.info("Reposititory manager does not exists for reporistory :"
					+ repoId);
			return;
		}
		if (manager.isBeingIndexed()) {
			log.info("The repo " + repoId
					+ " is being indexed. Terminating it...");
			manager.terminate();
			try {
				manager.getIndexingThread().join();
			} catch (InterruptedException e) {
				log.error("RepoID:" + repoId + ". Error: " + e.getMessage());
			}
		}
	}

	synchronized public void terminate() {
		Collection<SubversionManager> repositories = multipleSubversionRepositoryManager
				.getRepositoryList();
		log.info("Terminating the revision indexer...");
		for (SubversionManager manager : repositories) {
			manager.terminate();
		}
		log.info("... revision indexer terminated");
	}

}
