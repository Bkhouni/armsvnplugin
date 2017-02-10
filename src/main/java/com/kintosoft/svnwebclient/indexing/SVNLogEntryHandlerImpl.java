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
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.util.JiraKeyUtils;
import com.kintosoft.jira.plugin.ext.subversion.SubversionManager;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.graph.entities.managers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class SVNLogEntryHandlerImpl implements ISVNLogEntryHandler,
		ISVNCanceller {

	private final static Logger log = LoggerFactory
			.getLogger(SVNLogEntryHandlerImpl.class);

	private static final int MAX_VARCHAR_LENGHT = 65535;


	//VALIANTYS - DONE
	private static String sqlCopyInsert = "insert into COPIES (REVISION,ITEMID,FROM_REVISION, FROM_ITEMID, REPOID) VALUES(?,?,?,?,?)";
	private static String sqlCommentInsert = "insert into COMMENTS (REVISION,COMMENT, REPOID) VALUES(?,?,?)";
	private static String sqlRevisionInsert = "insert into REVISIONS (REVISION,AUTHOR,TIMESTAMP, DAY, REPOID) VALUES(?,?,?,?,?)";
	private static String sqlActionInsert = "insert into ACTIONS (REVISION,ACTION,ITEMID, REPOID) VALUES(?,?,?,?)";
	private static String sqlItemInsert = "insert into ITEMS (PATH,NAME, REPOID) values (?,?,?)";
	private static String sqlItem = "select * from ITEMS where PATH=? AND NAME=? AND REPOID=?";
	private static String sqlKeyInsert = "insert into KEYS (REVISION, PROJECT, ISSUE, REPOID) values(?,?,?,?)";

	private PreparedStatement pstCopyInsert;
	private PreparedStatement pstCommentInsert;
	private PreparedStatement pstRevisionInsert;
	private PreparedStatement pstActionInsert;
	private PreparedStatement pstItemInsert;
	private PreparedStatement pstItem;
	private PreparedStatement pstKeyInsert;

	private final RevisionIndexer indexer;
	private final SubversionManager manager;
	private final long headRevision;
	private Connection conn;


	public static ActiveObjects activeObjects;

	private boolean dispose = false;

	public SVNLogEntryHandlerImpl(ActiveObjects activeObjects,RevisionIndexer indexer,
			SubversionManager manager, long headRevision) {
		this.indexer = indexer;
		this.manager = manager;
		this.headRevision = headRevision;
		SVNLogEntryHandlerImpl.activeObjects = activeObjects;
	}

	public void start() throws SQLException {
		/*conn = indexer.getConnetion();
		createPreparedStatements(conn);*/
	}

	public void stop() throws SQLException {
		/*try {
			closePreparedStatements();
		} finally {
			PluginConnectionPool.closeConnection(conn);
		}*/
	}

	private void createPreparedStatements(Connection conn) throws SQLException {
		pstActionInsert = conn.prepareStatement(sqlActionInsert);
		pstCommentInsert = conn.prepareStatement(sqlCommentInsert);
		pstCopyInsert = conn.prepareStatement(sqlCopyInsert);
		pstItem = conn.prepareStatement(sqlItem);
		pstItemInsert = conn.prepareStatement(sqlItemInsert);
		pstRevisionInsert = conn.prepareStatement(sqlRevisionInsert);
		pstKeyInsert = conn.prepareStatement(sqlKeyInsert);
	}

	protected void closePreparedStatements() throws SQLException {

		pstActionInsert.close();

		pstCommentInsert.close();

		pstCopyInsert.close();

		pstItem.close();

		pstItemInsert.close();

		pstRevisionInsert.close();

		pstKeyInsert.close();

	}

	private void addCopy(long repoId, long rev, long itemId, long fromRev,
			String fromPath, String fromName) throws SQLException {

		Repository repo = activeObjects.get(Repository.class, (int)repoId);
		Revision revision = activeObjects.get(Revision.class, (int) rev);
		Item item = activeObjects.get(Item.class, (int) itemId);
		Revision fromrevision = activeObjects.get(Revision.class, (int) fromRev);

		CopyManager copyManager = new CopyManager(activeObjects);
		ItemManager itemManager = new ItemManager(activeObjects);

		Item fromitem = itemManager.addItem(repo, fromName, fromPath);

		copyManager.addCopy(repo, revision, item, fromrevision, fromitem);

		/*long fromId = addItem(repoId, fromPath, fromName);
		pstCopyInsert.setLong(1, rev);
		pstCopyInsert.setLong(2, itemId);
		pstCopyInsert.setLong(3, fromRev);
		pstCopyInsert.setLong(4, fromId);
		pstCopyInsert.setLong(5, repoId);
		pstCopyInsert.execute();*/
	}

	private void addComment(long repoId, long rev, String comment)
			throws SQLException {

		if (comment != null && comment.length() > MAX_VARCHAR_LENGHT) {
			log.warn("Comment for revision " + rev + " truncated:"
					+ comment.length() + " bytes");
			comment = comment.substring(0, MAX_VARCHAR_LENGHT);
		}

		Repository repo = activeObjects.get(Repository.class, (int) repoId);

		Revision revision = null;

		Revision[] revisions = repo.getRevisions();

		log.warn("----------- NUMBER OF REVISIONS IS : " + revisions.length );
		if(revisions.length > 0)
			log.warn("--------- !!! Revision 1 is : " + revisions[0]);

		for(Revision revisionObject : revisions){

			if (revisionObject.getRevision() == rev)
				revision = revisionObject;

		}


		CommentManager commentManager = new CommentManager(activeObjects);
		commentManager.addComment(repo, revision, comment);

		/*pstCommentInsert.setLong(1, rev);
		pstCommentInsert.setString(2, comment);
		pstCommentInsert.setLong(3, repoId);
		pstCommentInsert.execute();*/
	}

	public void addRevision(long repoId, long number, String author, Date date)
			throws SQLException {

		Repository repo = activeObjects.get(Repository.class, (int) repoId);
		RevisionManager revisionManager = new RevisionManager(activeObjects);

		/*pstRevisionInsert.setLong(1, number);
		pstRevisionInsert.setString(2, author);*/
		if (date != null) {

			revisionManager.addRevision(number,repo, author,new Timestamp(date.getTime()),new java.util.Date(date.getTime()));

			/*pstRevisionInsert.setTimestamp(3, new Timestamp(date.getTime()));
			pstRevisionInsert.setDate(4, new java.sql.Date(date.getTime()));*/
		} else {

			revisionManager.addRevision(number,repo, author, null, null);

			/*pstRevisionInsert.setTimestamp(3, null);
			pstRevisionInsert.setDate(4, null);*/
		}
		/*pstRevisionInsert.setLong(5, repoId);
		pstRevisionInsert.execute();*/
	}

	public void addAction(long repoId, long rev, String action, long itemId)
			throws SQLException {


		Repository repo = activeObjects.get(Repository.class, (int) repoId);

		Item item = activeObjects.get(Item.class, (int) itemId);

		ActionManager actionManager = new ActionManager(activeObjects);

		Revision revision = null;

		Revision[] revisions = repo.getRevisions();
		for(Revision revisionObject : revisions){

			if (revisionObject.getRevision() == rev)
				revision = revisionObject;

		}

		actionManager.addAction(repo, revision, action, item);

		/*pstActionInsert.setLong(1, rev);
		pstActionInsert.setString(2, action);
		pstActionInsert.setLong(3, itemId);
		pstActionInsert.setLong(4, repoId);
		pstActionInsert.execute();*/
	}

	public int addItem(long repoId, String path, String name)
			throws SQLException {
		int id = getItemId(repoId, path, name);
		if (id != -1) {
			return id;
		}

		Repository repo = activeObjects.get(Repository.class, (int) repoId);

		ItemManager itemManager = new ItemManager(activeObjects);
		Item newItem = itemManager.addItem(repo, name, path);

		/*pstItemInsert.setString(1, path);
		pstItemInsert.setString(2, name);
		pstItemInsert.setLong(3, repoId);
		pstItemInsert.execute();*/

		return newItem.getID();
	}

	private int getItemId(long repoId, String path, String name)
			throws SQLException {

		Repository repo = activeObjects.get(Repository.class, (int) repoId);

		ItemManager itemManager = new ItemManager(activeObjects);
		Item item = itemManager.getItemByValues(repo, path, name);

		/*Long id = null;
		pstItem.setString(1, path);
		pstItem.setString(2, name);
		pstItem.setLong(3, repoId);
		ResultSet rs = pstItem.executeQuery();
		if (rs.next()) {
			id = rs.getLong("ID");
		}
		rs.close();*/

		if(item != null)
			return item.getID();
		else
			return -1;
	}

	public boolean addKey(long repoId, long rev, String project, long issue)
			throws SQLException {

		Repository repository = activeObjects.get(Repository.class, (int) repoId);

		KeyManager keyManager = new KeyManager(activeObjects);
		Key newkey = keyManager.addKey(repository, rev,project, issue);

		/*pstKeyInsert.setLong(1, rev);
		pstKeyInsert.setString(2, project);
		pstKeyInsert.setLong(3, issue);
		pstKeyInsert.setLong(4, repoId);*/
		return (newkey != null);
	}

	@Override
	public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
		checkCancelled();
		long repoId = manager.getId();
		Long rev = logEntry.getRevision() ;
		String auth = logEntry.getAuthor();
		Date date = logEntry.getDate();
		String comment = logEntry.getMessage();
		if (comment == null) {
			comment = "";
		}
		comment = comment.replaceAll("\r\n", "\n");

		try {
			addRevision(repoId, rev, auth, date);
			List<String> issueKeys = JiraKeyUtils
					.getIssueKeysFromString(comment.toUpperCase());

			List<String> addedKeys = new ArrayList<String>();

			for (String issueKey : issueKeys) {
				String projectKey = IssueKey.from(issueKey).getProjectKey();
				long countKey = IssueKey.from(issueKey).getIssueNumber();
				issueKey = projectKey + "-" + countKey;
				if (addedKeys.contains(issueKey)) {
					continue;
				}
				addKey(repoId, rev, projectKey, countKey);
				addedKeys.add(issueKey);

			}

			addComment(repoId, rev, comment);

			Map<String, SVNLogEntryPath> paths = logEntry.getChangedPaths();
			for (Iterator<Map.Entry<String, SVNLogEntryPath>> it2 = paths
					.entrySet().iterator(); it2.hasNext();) {
				SVNLogEntryPath logPath = it2.next().getValue();

				char action = logPath.getType();
				String path = logPath.getPath();

				int i = path.lastIndexOf("/") + 1;
				String name = path.substring(i, path.length());
				path = path.substring(0, i);

				long itemId = addItem(repoId, path, name);
				addAction(repoId, rev, Character.toString(action), itemId);

				String fromPath = logPath.getCopyPath();
				if (fromPath != null) {
					long fromRev = logPath.getCopyRevision();

					i = fromPath.lastIndexOf("/") + 1;
					String fromName = fromPath.substring(i, fromPath.length());
					fromPath = fromPath.substring(0, i);

					addCopy(repoId, rev, itemId, fromRev, fromPath, fromName);
				}

			}
			/*conn.commit();*/
			log.info("Repository '" + repoId + "' synchronized to revision "
					+ rev);

			if (rev == headRevision) {
				dispose = true;
				stop();
			}
		} catch (SQLException ex) {
			String errMsg;
			if (dispose) {
				errMsg = "RepoId='" + repoId + "' indexation canceled";
			} else {
				errMsg = "RepoId='" + repoId + "' " + ex.getMessage();
			}
			try {
				stop();
			} catch (Exception e) {

			}

			SVNErrorMessage em = SVNErrorMessage.create(SVNErrorCode.UNKNOWN,
					errMsg);
			throw new SVNCancelException(em);
		}
	}

	synchronized public void terminate() {
		dispose = true;
	}

	@Override
	public void checkCancelled() throws SVNCancelException {
		if (dispose) {
			SVNErrorMessage em = SVNErrorMessage.create(SVNErrorCode.UNKNOWN,
					"Repository " + manager.getId() + " indexation canceled");
			throw new SVNCancelException(em);
		}
	}
}
