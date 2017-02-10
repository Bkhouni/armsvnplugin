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
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.kintosoft.svnwebclient.db.DatabaseFunctions;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import com.kintosoft.svnwebclient.graph.entities.ao.*;
import com.kintosoft.svnwebclient.graph.model.*;
import com.kintosoft.svnwebclient.graph.model.jira.Issue;
import com.kintosoft.svnwebclient.graph.model.jira.KeyModel;
import com.kintosoft.svnwebclient.indexing.RevisionIndexer;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

final class DBGraph {

	private static Logger log = LoggerFactory.getLogger(DBGraph.class);

	private boolean isDir;


	private final ActiveObjects ao;


	private static class DBAction {
		String actId;
		long actRev;
		String actPath;
		String actName;

		DBAction(Action action) throws SQLException {
			actRev = action.getRevision().getRevision();
			actId = action.getAction();
			actPath = action.getItem().getPath();
			actName = action.getItem().getName();
		}
	}

    private final String DATE_FORMAT = "yyyy-MM-dd";

	public DBGraph( ActiveObjects ao){
		this.ao = ao;
	}
	/*private Connection getConnection() throws SQLException {
		return PluginConnectionPool.getConnection();
	}*/

	private void closeConnection(Connection c) throws SQLException {
		PluginConnectionPool.closeConnection(c);
	}

	public Graph buildGraph(long repoId, String item, long pegRevision,
			boolean isDir, boolean addEdges, boolean strict,
			boolean loadMetadata, boolean loadSegmentActions) throws Exception {

		this.isDir = isDir;

		int index = item.lastIndexOf("/") + 1;
		String parent = item.substring(0, index);
		String child = item.substring(index, item.length());
		// System.out.println(item + "@" + pegRevision);
		// System.out.println("=======================================");
		Graph graph = new Graph();

		graph.tagsName = SWCUtils.getConfigurationProvider(repoId)
				.getTagsName();

		Connection conn = null;

		try {

			/*conn = getConnection();*/
			buildSegmentsRecursively(conn, repoId, parent, child, pegRevision,
					graph, loadSegmentActions);

			strictCopySources(graph, strict);

			// nodes might be non ordered because the just added G
			for (ItemModel i : graph.items.values()) {
				for (Segment s : i.segments.values()) {
					s.sort();
				}
			}

			if (addEdges) {
				addEdges(graph);
				// identifyRenamed(graph);
				// removeEdges(graph);
			}
			if (loadMetadata) {
				loadMetadata(conn, repoId, graph);
			}

			metaTags(graph);

		} finally {
			closeConnection(conn);
		}

		return graph;
	}

	public List<ActionModel> revisionsInRangePopulated(long repoId, String path,
													   String name, long startRev, long endRev, boolean isDir)
			throws Exception {
		Connection conn = null;
		try {
			/*conn = getConnection();*/
			List<DBActionRevision> res = revisionsInRange(conn, repoId, path,
					name, startRev, endRev, isDir);
			List<ActionModel> actions = new ArrayList<ActionModel>();
			Graph graph = new Graph();
			for (DBActionRevision dbar : res) {
				actions.add(graph.getFooAction(dbar.action, dbar.revision));
			}
			List<Long> alreaPopulated = new ArrayList<Long>();
			for (ActionModel act : actions) {
				if (alreaPopulated.contains(act.revision.number)) {
					continue;
				}
				alreaPopulated.add(act.revision.number);
				loadMetadata(conn, repoId, act.revision);
			}
			return actions;
		} finally {
			closeConnection(conn);
		}
	}

	protected void metaTags(Graph graph) {
		for (ItemModel item : graph.items.values()) {
			String itemPath = item.parent + item.child;
			item.meta_tag = (itemPath.indexOf("/" + graph.tagsName + "/") > -1 || itemPath
					.endsWith("/" + graph.tagsName));
		}
	}

	protected void strictCopySources(Graph graph, boolean strict)
			throws Exception {
		for (CopyModel copy : graph.copies) {
			ItemModel fromItem = copy.fromItem, toItem = copy.toItem;
			RevisionModel fromRev = copy.fromRevision, toRev = copy.toRevision;
			Segment fromSegment = fromItem.getSegment(fromRev.number), toSegment = toItem
					.getSegment(toRev.number);

			ActionModel fromAct = graph.actions.get(ActionModel.getActionKey(
					fromItem.parent, fromItem.child, fromRev.number));

			if (fromAct == null) {
				if (strict) {
					fromAct = fromSegment.getClosestAction(fromRev.number);
				} else {
					// might unsort the segment
					fromAct = graph.action("G", fromRev.number, fromSegment);
				}
			}

			ActionModel toAct = graph.actions.get(ActionModel.getActionKey(toItem.parent,
					toItem.child, toRev.number));
			copy.fromRevision = fromAct.revision;
			copy.toRevision = toAct.revision;
		}
	}

	protected void identifyRenamed(Graph graph) {
		for (ActionModel act : graph.actions.values()) {
			if (act.id.equals("D")) {
				for (ActionModel sibling : act.parent.children) {
					if (sibling.id.equals("A")
							&& act.revision.number == sibling.revision.number) {
						sibling.segment.meta_renamed = act.segment.item;
						break;
					}
				}
			}
		}
	}

	protected void addEdges(Graph graph) {

		for (CopyModel copy : graph.copies) {

			ItemModel fromItem = copy.fromItem;
			ItemModel toItem = copy.toItem;

			RevisionModel fromRev = copy.fromRevision;
			RevisionModel toRev = copy.toRevision;

			ActionModel fromAct = graph.actions.get(ActionModel.getActionKey(
					fromItem.parent, fromItem.child, fromRev.number));
			ActionModel toAct = graph.actions.get(ActionModel.getActionKey(toItem.parent,
					toItem.child, toRev.number));

			fromAct.children.add(toAct);
			toAct.parent = fromAct;
		}

		for (ItemModel item : graph.items.values()) {
			for (Segment seg : item.segments.values()) {

				ActionModel parent = null;
				for (ActionModel act : seg.actions) {
					if (parent == null) {
						parent = act;
						continue;
					}
					act.parent = parent;
					parent.children.add(act);
					parent = act;
				}
			}

		}

	}

	protected void removeEdges(Graph graph) {
		for (ActionModel act : graph.actions.values()) {
			act.parent = null;
			act.children.clear();
		}
	}

	private String[] getItemPathAndNameFromId(Connection c, long repoId,
			long itemId) throws SQLException {
		String sqlItemFromID = "select PATH, NAME from ITEMS where repoId=? and id=?";

		String[] res = new String[2];
		Item item = this.ao.get(Item.class,(int) itemId);
		res[0] = item.getPath();
		res[1] = item.getName();

		/*PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sqlItemFromID);
			ps.setLong(1, repoId);
			ps.setLong(2, itemId);
			log.debug(sqlItemFromID);
			ResultSet rs = ps.executeQuery();
			rs.next();
			res[0] = rs.getString("PATH");
			res[1] = rs.getString("NAME");
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		return res;
	}

	public void buildSegmentsRecursively(Connection conn, long repoId,
			String path, String name, long pegRevision, Graph graph,
			boolean loadSegmentActions) throws Exception {

		DBAction foundBackaward = getNearestBackwardParentAddedRevision(conn,
				repoId, path, name, pegRevision);

		long startRev = foundBackaward.actRev;

		long pegRev2 = pegRevision;

		// do not look for forward in the start revision as it might be a
		// replacement.
		if (pegRevision == startRev) {
			pegRev2++;
		}

		DBAction foundForward = getNearestForwardParentDeletedRevision(conn,
				repoId, path, name, pegRev2);

		long endRev;
		if (foundForward == null) {
			endRev = (long) Integer.MAX_VALUE;
		} else {
			endRev = foundForward.actRev;
		}

		ItemModel item = graph.getItem(path, name);

		Segment segment = graph.getSegment(item, startRev, endRev);

		// actions in segments are sorted

		// we do not need to check whether it will be duplicated or not ->
		// validated later
		graph.action(foundBackaward.actId, foundBackaward.actRev, segment);

		List<DBActionRevision> res;

		if (loadSegmentActions) {
			res = revisionsInRange(conn, repoId, path, name, startRev, endRev,
					isDir);
		} else {
			res = new ArrayList<DBGraph.DBActionRevision>();
		}

		for (DBActionRevision ar : res) {
			String actionId = ar.action;
			graph.action(actionId, ar.revision, segment);
		}

		if (foundForward != null) {
			graph.action(foundForward.actId, foundForward.actRev,
					segment);
			segment.meta_deleted = true;
		}

		List<List<CopyEdge>> branches = branches(conn, repoId, startRev,
				endRev, path, name, graph);

		List<CopyEdge> branchesBackward = branches.get(0);

		List<CopyEdge> branchesForward = branches.get(1);

		for (CopyEdge ce : branchesForward) {
			graph.copy(ce.fromPath, ce.fromName, ce.fromRev, ce.toPath,
					ce.toName, ce.toRev);
			if (segmentAlreadyProcessed(graph, ce.toPath, ce.toName, ce.toRev)) {
				continue;
			}
			buildSegmentsRecursively(conn, repoId, ce.toPath, ce.toName,
					ce.toRev, graph, loadSegmentActions);
		}

		for (CopyEdge ce : branchesBackward) {
			graph.copy(ce.fromPath, ce.fromName, ce.fromRev, ce.toPath,
					ce.toName, ce.toRev);
			if (segmentAlreadyProcessed(graph, ce.fromPath, ce.fromName,
					ce.fromRev)) {
				continue;
			}
			buildSegmentsRecursively(conn, repoId, ce.fromPath, ce.fromName,
					ce.fromRev, graph, loadSegmentActions);
		}
	}

	private boolean segmentAlreadyProcessed(Graph graph, String path,
			String name, long rev) {
		ItemModel item = graph.getItem(path, name);

		return item.segmentExists(rev);
	}

	private class DBActionRevision {
		@Override
		public String toString() {
			return action + " @" + revision;
		}

		String action;
		Long revision;
	}

	private List<DBActionRevision> revisionsInRange(Connection c, long repoId,
			String path, String name, long startRev, long endRev, boolean isDir)
			throws Exception {

		String sql;
		Query query;

		if (isDir) {
			sql = "select a.revision, action"
					+ " from ACTIONS as a, ITEMS as i"
					+ " where a.ITEMID=i.ID  AND a.REVISION between ? and ? and a.repoId=i.repoId and i.repoId=? "
					+ " and ((i.PATH=? and i.NAME=?) or (i.PATH LIKE ?)) order by a.revision ASC";
			query = Query.select().alias(Action.class, "a").alias(Item.class, "i")
					.join(Item.class,"a.REPO_ID = i.REPO_ID AND a.ITEM_ID = i.ID ")
					.where("i.REPO_ID = ? AND ((i.PATH=? and i.NAME=?) or (i.PATH LIKE ?)) AND a.REVISION_ID BETWEEN ? AND ? ",repoId, path, name, startRev, endRev)
					.order("a.REVISION ASC");

		} else {
			sql = "select a.revision, action"
					+ " from ACTIONS as a, ITEMS as i"
					+ " where a.ITEMID=i.ID  AND a.REVISION between ? and ? and a.repoId=i.repoId and i.repoId=? "
					+ " and i.PATH=? and i.NAME=? order by a.revision ASC";

			query = Query.select().alias(Action.class, "a").alias(Item.class, "i")
					.join(Item.class,"a.REPO_ID = i.REPO_ID AND a.ITEM_ID = i.ID ")
					.where("i.REPO_ID = ? AND i.PATH=? and i.NAME=? AND a.REVISION_ID BETWEEN ? AND ? ",repoId, path, name, startRev, endRev)
					.order("a.REVISION ASC");
		}

		List<DBActionRevision> res = new ArrayList<DBActionRevision>();


		List<Action> actions = newArrayList(this.ao.find(Action.class, query));

		for(Action action:actions) {

			DBActionRevision ar = new DBActionRevision();
			ar.action = action.getAction();
			ar.revision = action.getRevision().getRevision();
			if (isDir) {
				if (ar.revision > startRev && ar.revision < endRev) {
					ar.action = "M";
				}

			}
			res.add(ar);
		}


/*
		PreparedStatement ps = null;
		int i = 0;
		try {
			ps = c.prepareStatement(sql);
			ps.setLong(++i, startRev);
			ps.setLong(++i, endRev);
			ps.setLong(++i, repoId);
			ps.setString(++i, path);
			ps.setString(++i, name);
			if (isDir) {
				ps.setString(++i, path + name + "/%");
			}

			log.debug(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				DBActionRevision ar = new DBActionRevision();
				ar.action = rs.getString("ACTION");
				ar.revision = rs.getLong("REVISION");
				if (isDir) {
					if (ar.revision > startRev && ar.revision < endRev) {
						ar.action = "M";
					}

				}
				res.add(ar);
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/

		return res;

	}

	private List<List<CopyEdge>> branches(Connection conn, long repoId,
			long startRev, long endRev, String path, String name, Graph graph)
			throws Exception {

		final String item = path + name;

		List<List<CopyEdge>> res = new ArrayList<List<CopyEdge>>();
		List<CopyEdge> branchesBackward = new ArrayList<CopyEdge>();
		List<CopyEdge> branchesForward = new ArrayList<CopyEdge>();
		res.add(branchesBackward);
		res.add(branchesForward);

		String fullPath = path + name;
		StringTokenizer st = new StringTokenizer(fullPath, "/");
		String grandFather = "/";
		while (st.hasMoreTokens()) {
			String entry = st.nextToken();
			grandFather += entry + "/";

			String tmp = grandFather.substring(0, grandFather.length() - 1);
			int index = tmp.lastIndexOf("/") + 1;
			String parentPath = tmp.substring(0, index);
			String parentName = tmp.substring(index, tmp.length());

			String remanent = fullPath.substring(tmp.length(),
					fullPath.length());
			CopyEdge back = branchBackward(conn, repoId, parentPath,
					parentName, remanent, startRev, graph);
			if (back != null) {
				branchesBackward.add(back);
			}
			List<CopyEdge> fwd = branchesForward(conn, repoId, parentPath,
					parentName, remanent, startRev, endRev, graph);

			branchesForward.addAll(fwd);
		}

		return res;
	}

	private static class CopyEdge {
		String fromPath;
		String fromName;
		Long fromRev;

		String toPath;
		String toName;
		Long toRev;

		public CopyEdge(String fromPath, String fromName, Long fromRev,
				String toPath, String toName, Long toRev) {
			this.fromPath = fromPath;
			this.fromName = fromName;
			this.fromRev = fromRev;

			this.toPath = toPath;
			this.toName = toName;
			this.toRev = toRev;
		}

		static boolean conflictTo(CopyEdge ce1, CopyEdge ce2) {
            return ce1.toRev.equals(ce2.toRev) && ce1.toPath.equals(ce2.toPath)
                    && ce1.toName.equals(ce2.toName);

        }

		@Override
		public String toString() {
			return fromPath + fromName + "@" + fromRev + " -> " + toPath
					+ toName + "@" + toRev;
		}

		boolean isChildToOf(CopyEdge parent) {
			return (parent.toPath + parent.toName + "/").equals(this.toPath);
		}

	}

	private CopyEdge branchBackward(Connection c, long repoId, String toPath,
			String toName, String remanent, long rev, Graph graph)
			throws Exception {

		String sqlBranchBack = "select * from COPIES as c, ITEMS as i where c.ITEMID=i.ID and i.PATH=? and i.NAME=? and c.REVISION =? and i.repoId=?";

		Query query = Query.select().alias(Copy.class, "c").alias(Item.class, "i")
				.join(Item.class,"c.ITEM_ID = i.ID")
				.where("i.PATH = ? AND i.NAME = ? AND c.REVISION_ID = ? AND i.REPO_ID = ?", toPath, toName, rev, repoId);

		List<Copy> copies = newArrayList(this.ao.find(Copy.class,query));

		CopyEdge found = null;
		for(Copy copy: copies) {
			long fromItemId = copy.getFromItem().getID();
			String[] fromItemFields = getItemPathAndNameFromId(c, repoId,
					fromItemId);
			String fromPath = fromItemFields[0];
			String fromName = fromItemFields[1];
			long fromRev = copy.getFromRevision().getRevision();

			long toRev = copy.getRevision().getID();

			String fullFromPath = fromPath + fromName + remanent;
			int index = fullFromPath.lastIndexOf("/") + 1;
			String fromPath2 = fullFromPath.substring(0, index);
			String fromName2 = fullFromPath.substring(index,
					fullFromPath.length());

			String fullToPath = toPath + toName + remanent;
			index = fullToPath.lastIndexOf("/") + 1;
			String toPath2 = fullToPath.substring(0, index);
			String toName2 = fullToPath.substring(index,
					fullToPath.length());

			CopyEdge candidate = new CopyEdge(fromPath2, fromName2,
					fromRev, toPath2, toName2, toRev);

			if (found == null) {
				found = candidate;
			} else {
				if (found.fromRev > candidate.fromRev) {
					found = candidate;
				}
			}
		}

		/*PreparedStatement ps = null;
		CopyEdge found = null;
		try {
			ps = c.prepareStatement(sqlBranchBack);
			ps.setString(1, toPath);
			ps.setString(2, toName);
			ps.setLong(3, rev);
			ps.setLong(4, repoId);

			log.debug(sqlBranchBack);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				long fromItemId = rs.getLong("FROM_ITEMID");
				String[] fromItemFields = getItemPathAndNameFromId(c, repoId,
						fromItemId);
				String fromPath = fromItemFields[0];
				String fromName = fromItemFields[1];
				long fromRev = rs.getLong("FROM_REVISION");

				long toRev = rs.getLong("REVISION");

				String fullFromPath = fromPath + fromName + remanent;
				int index = fullFromPath.lastIndexOf("/") + 1;
				String fromPath2 = fullFromPath.substring(0, index);
				String fromName2 = fullFromPath.substring(index,
						fullFromPath.length());

				String fullToPath = toPath + toName + remanent;
				index = fullToPath.lastIndexOf("/") + 1;
				String toPath2 = fullToPath.substring(0, index);
				String toName2 = fullToPath.substring(index,
						fullToPath.length());

				CopyEdge candidate = new CopyEdge(fromPath2, fromName2,
						fromRev, toPath2, toName2, toRev);

				if (found == null) {
					found = candidate;
				} else {
					if (found.fromRev > candidate.fromRev) {
						found = candidate;
					}
				}
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		return found;
	}

	private List<CopyEdge> branchesForward(Connection c, long repoId,
			String fromPath, String fromName, String remanent, long startRev,
			long endRev, Graph graph) throws Exception {

		List<CopyEdge> candidates = new ArrayList<CopyEdge>();
		String sqlBranchFwd = "select * from COPIES as c, ITEMS as i where c.FROM_ITEMID=i.ID and i.PATH=? and i.NAME=? and c.FROM_REVISION between ? and ? and i.repoId=?";

		Query query = Query.select().alias(Copy.class,"c").alias(Item.class,"i")
				.join(Item.class,"c.FROMITEM_ID=i.ID")
				.where("i.PATH=? and i.NAME=? and c.FROMREVISION_ID between ? and ? and i.REPO_ID= ? ", fromPath, fromName, startRev, endRev, repoId);

		List<Copy> copies = newArrayList(this.ao.find(Copy.class, query));

		for(Copy copy : copies) {
			long fromRev = copy.getFromRevision().getID();

			long toItemId = copy.getItem().getID();
			String[] toItemFields = getItemPathAndNameFromId(c, repoId,
					toItemId);
			String toPath = toItemFields[0];
			String toName = toItemFields[1];
			long toRev = copy.getRevision().getRevision();

			String fullFromPath = fromPath + fromName + remanent;
			int index = fullFromPath.lastIndexOf("/") + 1;
			String fromPath2 = fullFromPath.substring(0, index);
			String fromName2 = fullFromPath.substring(index,
					fullFromPath.length());

			String fullToPath = toPath + toName + remanent;
			index = fullToPath.lastIndexOf("/") + 1;
			String toPath2 = fullToPath.substring(0, index);
			String toName2 = fullToPath.substring(index,
					fullToPath.length());

			CopyEdge candidate = new CopyEdge(fromPath2, fromName2,
					fromRev, toPath2, toName2, toRev);
			candidates.add(candidate);
		}

		/*PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sqlBranchFwd);
			ps.setString(1, fromPath);
			ps.setString(2, fromName);
			ps.setLong(3, startRev);
			ps.setLong(4, endRev);
			ps.setLong(5, repoId);

			log.debug(sqlBranchFwd);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				long fromRev = rs.getLong("FROM_REVISION");

				long toItemId = rs.getLong("ITEMID");
				String[] toItemFields = getItemPathAndNameFromId(c, repoId,
						toItemId);
				String toPath = toItemFields[0];
				String toName = toItemFields[1];
				long toRev = rs.getLong("REVISION");

				String fullFromPath = fromPath + fromName + remanent;
				int index = fullFromPath.lastIndexOf("/") + 1;
				String fromPath2 = fullFromPath.substring(0, index);
				String fromName2 = fullFromPath.substring(index,
						fullFromPath.length());

				String fullToPath = toPath + toName + remanent;
				index = fullToPath.lastIndexOf("/") + 1;
				String toPath2 = fullToPath.substring(0, index);
				String toName2 = fullToPath.substring(index,
						fullToPath.length());

				CopyEdge candidate = new CopyEdge(fromPath2, fromName2,
						fromRev, toPath2, toName2, toRev);
				candidates.add(candidate);
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/

		return candidates;

	}

	private void loadMetadata(Connection conn, long repoId, Graph graph)
			throws Exception {
		for (RevisionModel rev : graph.revisions.values()) {
			loadMetadata(conn, repoId, rev);
		}
	}

	private void loadMetadata(Connection conn, long repoId, RevisionModel rev)
			throws Exception {
		loadAuthorDateComment(conn, repoId, rev);
		loadTickets(conn, repoId, rev);
	}

	private void loadTickets(Connection c, long repoId, RevisionModel rev)
			throws SQLException {
		String sql = "select PROJECT, ISSUE from KEYS where repoId=? AND revision=?";

		Query query = Query.select().where("REPO_ID = ? AND REVISION_ID= ? ", repoId, rev.number);
		List<Key> keys = newArrayList(this.ao.find(Key.class, query));

		for(Key key : keys) {
			String issueKey = key.getProject() + "-"
					+ key.getIssue();
			Issue issue = SWCUtils.getIssue(issueKey);
			if (issue != null) {
				rev.tickets.add(issue);
			}
		}


		/*PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sql);
			ps.setLong(1, repoId);
			ps.setLong(2, rev.number);
			log.debug(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String issueKey = rs.getString("PROJECT") + "-"
						+ rs.getString("ISSUE");
				Issue issue = SWCUtils.getIssue(issueKey);
				if (issue != null) {
					rev.tickets.add(issue);
				}
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
	}

	private RevisionModel loadAuthorDateComment(Connection c, long repoId,
												RevisionModel rev) throws Exception {

/*		String sqlAuthAndDateFromRev = "select AUTHOR,RTIMESTAMP, COMMENT"
				+ " from REVISIONS as R, COMMENTS as C "
				+ "where c.REVISION=r.REVISION AND c.REPOID=r.REPOID and r.REVISION=? and r.repoId=?";*/

		Query query = Query.select().alias(Revision.class, "r").alias(Comment.class, "c")
				.join(Revision.class,"c.REVISION_ID=r.REVISION_ID AND c.REPO_ID=r.REPO_ID")
				.where("r.ID = ? AND r.REPO_ID = ? ", rev.number, repoId);

		List<Comment> comments = newArrayList(this.ao.find(Comment.class, query));
		if (comments.size() > 0) {
			rev.author = comments.get(0).getRevision().getAuthor();
			Timestamp t = comments.get(0).getRevision().getRTimestamp();
			rev.date = t == null ? null : new Date(t.getTime());
			rev.comment = comments.get(0).getComment();
		}
/*
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sqlAuthAndDateFromRev);
			ps.setLong(1, rev.number);
			ps.setLong(2, repoId);
			log.debug(sqlAuthAndDateFromRev);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rev.author = rs.getString("AUTHOR");
				Timestamp t = rs.getRTimestamp("TIMESTAMP");
				rev.date = t == null ? null : new Date(t.getTime());
				rev.comment = rs.getString("COMMENT");
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		return rev;
	}

	// not always all the metadata (comment) is required -> statistics
	private RevisionModel loadAuthAndDateRevision(Connection c, long repoId,
												  RevisionModel rev) throws Exception {

		String sqlAuthAndDateFromRev = "select AUTHOR,RTIMESTAMP from REVISIONS where REVISION=? and repoId=?";

		Query query = Query.select().where("REVISION _ID = ? and REPO_ID = ? ", rev.number, repoId);
		List<Revision> revisions = newArrayList(this.ao.find(Revision.class, query));

		if (revisions.size() > 0) {
			rev.author = revisions.get(0).getAuthor();
			Timestamp t = revisions.get(0).getRTimestamp();
			rev.date = t == null ? null : new Date(t.getTime());
		}

		/*PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sqlAuthAndDateFromRev);
			ps.setLong(1, rev.number);
			ps.setLong(2, repoId);
			log.debug(sqlAuthAndDateFromRev);
			ResultSet rs = ps.executeQuery();
			rs.next();

			rev.author = rs.getString("AUTHOR");
			Timestamp t = rs.getRTimestamp("TIMESTAMP");
			rev.date = t == null ? null : new Date(t.getTime());
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		return rev;

	}

	private RevisionModel loadComment(Connection c, long repoId, RevisionModel rev)
			throws Exception {

		String sqlComment = "select COMMENT from COMMENTS where REVISION=? and repoId=?";

		Query query = Query.select().where("REVISION_ID = ? and REPO_ID = ? ",rev.number, repoId);
		List<Comment> comments = newArrayList(this.ao.find(Comment.class, query));

		/*PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sqlComment);
			ps.setLong(1, rev.number);
			ps.setLong(2, repoId);
			log.debug(sqlComment);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rev.comment = rs.getString("COMMENT");
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		return rev;

	}

	/* returns: actionId, revisionNumber, path, name */
	private DBAction getNearestForwardParentDeletedRevision(Connection c,
			long repoId, String path, String name, long pegRevision)
			throws Exception {

		String sqlParentDel = "select * from ACTIONS as a, ITEMS as i where a.ITEMID=i.ID and i.PATH=? and i.NAME=? and (a.ACTION='D' or a.ACTION='R') and a.REVISION>=? and i.repoId=? order by a.REVISION ASC";

		Query query;

		List<Action> actions;

		DBAction found = null;
		StringTokenizer st = new StringTokenizer(path + name, "/");
		String grandFather = "/";

		while (st.hasMoreTokens()) {
			String entry = st.nextToken();
			grandFather += entry + "/";

			String tmp = grandFather.substring(0, grandFather.length() - 1);
			int index = tmp.lastIndexOf("/") + 1;
			String pathX = tmp.substring(0, index);
			String nameX = tmp.substring(index, tmp.length());

			query = Query.select().alias(Action.class, "a").alias(Item.class, "i")
					.join(Item.class, "a.ITEM_ID = i.ID")
					.where("i.PATH = ? and i.NAME = ? and (a.ACTION='D' or a.ACTION='R') and a.REVISION_ID >= ? and i.REPO_ID=?", pathX, nameX, pegRevision, repoId)
					.order("a.REVISION_ID ASC");

			actions = newArrayList(this.ao.find(Action.class, query));

			if (actions.size() > 0) {
				DBAction candidate = new DBAction(actions.get(0));
				if (found == null) {
					found = candidate;
				} else if (candidate.actRev < found.actRev) {
					found = candidate;
				}
			}

		}


		/*DBAction found = null;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sqlParentDel);

			StringTokenizer st = new StringTokenizer(path + name, "/");
			String grandFather = "/";
			while (st.hasMoreTokens()) {
				String entry = st.nextToken();
				grandFather += entry + "/";

				String tmp = grandFather.substring(0, grandFather.length() - 1);
				int index = tmp.lastIndexOf("/") + 1;
				String pathX = tmp.substring(0, index);
				String nameX = tmp.substring(index, tmp.length());

				ps.setString(1, pathX);
				ps.setString(2, nameX);
				ps.setLong(3, pegRevision);
				ps.setLong(4, repoId);
				ps.setMaxRows(1);
				// ps.setLong(3, startRev);
				// ps.setLong(4, endRev);

				log.debug(sqlParentDel);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					DBAction candidate = new DBAction(rs);
					if (found == null) {
						found = candidate;
					} else if (candidate.actRev < found.actRev) {
						found = candidate;
					}
				}

				rs.close();
			}
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/
		if (found == null) {
			return null;
		}

		return found;
	}

	private DBAction getNearestBackwardParentAddedRevision(Connection c,
			long repoId, String path, String name, long pegRevision)
			throws Exception {

		String sqlParentDel = "select * from ACTIONS as a, ITEMS as i where a.ITEMID=i.ID and i.PATH=? and i.NAME=? and (a.ACTION='A' or a.action='R') and a.REVISION<=? and i.repoId=? order by a.REVISION DESC";

		Query query;

		List<Action> actions;

		DBAction found = null;
		StringTokenizer st = new StringTokenizer(path + name, "/");
		String grandFather = "/";

		while (st.hasMoreTokens()) {
			String entry = st.nextToken();
			grandFather += entry + "/";

			String tmp = grandFather.substring(0, grandFather.length() - 1);
			int index = tmp.lastIndexOf("/") + 1;
			String pathX = tmp.substring(0, index);
			String nameX = tmp.substring(index, tmp.length());

			query = Query.select().alias(Action.class, "a").alias(Item.class, "i")
					.join(Item.class, "a.ITEM_ID = i.ID")
					.where("i.PATH = ? and i.NAME = ? and (a.ACTION='D' or a.ACTION='R') and a.REVISION_ID >= ? and i.REPO_ID=?", pathX, nameX, pegRevision, repoId)
					.order("a.REVISION_ID DESC");

			actions = newArrayList(this.ao.find(Action.class, query));

			if (actions.size() > 0) {
				DBAction candidate = new DBAction(actions.get(0));
				if (found == null) {
					found = candidate;
				} else if (candidate.actRev < found.actRev) {
					found = candidate;
				}
			}

		}


		/*DBAction found = null;
		PreparedStatement ps = null;
		try {
			log.debug(sqlParentDel);
			ps = c.prepareStatement(sqlParentDel);

			StringTokenizer st = new StringTokenizer(path + name, "/");
			String grandFather = "/";
			while (st.hasMoreTokens()) {
				String entry = st.nextToken();
				grandFather += entry + "/";

				String tmp = grandFather.substring(0, grandFather.length() - 1);
				int index = tmp.lastIndexOf("/") + 1;
				String pathX = tmp.substring(0, index);
				String nameX = tmp.substring(index, tmp.length());

				ps.setString(1, pathX);
				ps.setString(2, nameX);
				ps.setLong(3, pegRevision);
				ps.setLong(4, repoId);
				ps.setMaxRows(1);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					DBAction candidate = new DBAction(rs);
					if (found == null) {
						found = candidate;
					} else if (candidate.actRev > found.actRev) {
						found = candidate;
					}
				}

				rs.close();
			}
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/

		if (found == null) {

			throw new Exception(
					"Oldest ancestor cannot be found for the artifact: " + path
							+ name + "@" + pegRevision);

			// found = new Object[] { "A", 0L, "/", "" };
		}

		return found;
	}

	protected Map<String, Map<Date, Integer>> getHistorySizeGroupByUserDay(
			String item, long pegRevision, long repoId, boolean isDirectory)
			throws Exception {

		Connection conn = null;
		try {
			/*conn = getConnection();*/
			return getHistorySizeGroupByUserDay(item, pegRevision,
					repoId, isDirectory);
		} finally {
			/*closeConnection(conn);*/
		}

	}

	private Map<String, Map<Date, Integer>> getHistorySizeGroupByUserDay(
			Connection conn, String item, long pegRevision, long repoId,
			boolean isDirectory) throws Exception {
		if (!item.startsWith("/")) {
			item = "/" + item;
		}

		int i = item.lastIndexOf("/") + 1;
		String path = item.substring(0, i);
		String name = item.substring(i, item.length());

		DBAction startX = getNearestBackwardParentAddedRevision(conn, repoId,
				path, name, pegRevision);

		DBAction endX = getNearestForwardParentDeletedRevision(conn, repoId,
				path, name, pegRevision);

		Long endRev = (long) Integer.MAX_VALUE;

		if (endX != null) {
			endRev = endX.actRev;
		}

		long startRev = startX.actRev;

		Map<String, Map<Date, Integer>> sizes = getHistorySizeGroupByUserDay(
				repoId, path, name, startRev, endRev, isDirectory);

		String startItemAction = startX.actPath + startX.actName;

		// if the origin of the branch is not the item itself is because
		// the item was added because a copy of a parent directory, then it is
		// not found by direct access to the items table.
		if (!startItemAction.equals(item)) {
			addUserDay(conn, sizes, repoId, startRev);
		}

		if (endX != null) {
			String endItemAction = endX.actPath + endX.actName;
			if (!endItemAction.equals(item)) {
				addUserDay(conn, sizes, repoId, endRev);
			}
		}

		return sizes;
	}

	private void addUserDay(Connection conn,
			Map<String, Map<Date, Integer>> sizes, long repoId, long revision)
			throws Exception {
		RevisionModel revX = new RevisionModel(revision);
		loadAuthAndDateRevision(conn, repoId, revX);
		Date day = getDayFromTimeStamp(revX.date);
		if (day == null) {
			return;
		}
		if (revX.author == null) {
			revX.author = "<unknown>";
		}
		Map<Date, Integer> daySize = sizes.get(revX.author);
		if (daySize == null) {
			daySize = new HashMap<Date, Integer>();
			sizes.put(revX.author, daySize);
		}
		Integer size = daySize.get(daySize);
		if (size == null) {
			daySize.put(day, 1);
		} else {
			daySize.remove(day);
			daySize.put(day, size + 1);
		}
	}

	private void addDay(Connection conn, Map<Date, Integer> sizes, long repoId,
			long rev) throws Exception {
		Date day = getDayFromRevision(conn, repoId, rev);
		if (day == null) {
			return;
		}

		Integer size = sizes.get(day);

		if (size == null) {
			sizes.put(day, 1);
		} else {
			sizes.remove(day);
			sizes.put(day, size + 1);
		}
	}

	private Date getDayFromRevision(Connection conn, long repoId, long rev)
			throws Exception {
		RevisionModel startRevObj = new RevisionModel(rev);
		loadAuthAndDateRevision(conn, repoId, startRevObj);
		return getDayFromTimeStamp(startRevObj.date);
	}

	private Date getDayFromTimeStamp(Date t) throws ParseException {
		if (t == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String day = sdf.format(t);
		return sdf.parse(day);
	}

	private Map<String, Map<Date, Integer>> getHistorySizeGroupByUserDay(long repoId, String path, String name, long startRev,
			long endRev, boolean isDirectory) throws Exception {

		Map<String, Map<Date, Integer>> res = new HashMap<String, Map<Date, Integer>>();

		String sql = "select count(distinct r.revision) as size, day, author"
				+ " from REVISIONS as r, ACTIONS as a, ITEMS as i"
				+ " where r.RTIMESTAMP is not null and a.ITEMID=i.ID  AND a.REVISION between ? and ? and i.repoId=? and a.repoId=i.repoId and r.repoId=a.repoId and r.revision=a.revision"
				+ " and i.PATH=? and i.NAME=? group by author, day";

		String sql2 = "select count(distinct r.revision) as size, day, author"
				+ " from REVISIONS as r, ACTIONS as a, ITEMS as i"
				+ " where r.RTIMESTAMP is not null and a.ITEMID=i.ID  AND a.REVISION between ? and ? and i.repoId=? and a.repoId=i.repoId and r.repoId=a.repoId and r.revision=a.revision"
				+ " and ((i.PATH=? and i.NAME=?) or (i.PATH LIKE ?)) group by author, day";



		Query query = Query.select("r.AUTHOR, r,DAY").alias(Revision.class, "r").alias(Action.class, "a").alias(Item.class, "i")
				.join(Action.class,"r.REPO_ID = a.REPO_ID AND r.REVISION_ID = a.REVISION_ID")
				.join(Item.class, "a.ITEM_ID = i.ID AND a.REPO_ID = i.REPO_ID")
				.where("r.RTIMESTAMP is not null AND a.REVISION_ID between ? AND ? AND i.REPO_ID = ? AND i.PATH = ? AND i.NAME = ?",startRev, endRev, repoId, path, name)
				.group("r.AUTHOR, r,DAY");

		Query query2 = Query.select("r.AUTHOR, r,DAY").alias(Revision.class, "r").alias(Action.class, "a").alias(Item.class, "i")
				.join(Action.class,"r.REPO_ID = a.REPO_ID AND r.REVISION_ID = a.REVISION_ID")
				.join(Item.class, "a.ITEM_ID = i.ID AND a.REPO_ID = i.REPO_ID")
				.where("r.RTIMESTAMP is not null AND a.REVISION_ID between ? AND ? AND i.REPO_ID = ? AND ((i.PATH = ? and i.NAME = ?) or (i.PATH LIKE ?))",startRev, endRev, repoId, path, name, path + name + (name.equals("") ? "" : "/"))
				.group("r.AUTHOR, r,DAY");



		if (isDirectory) {
			/*sql = sql2;*/
			query = query2;
		}

		List<Revision> revisions = newArrayList(this.ao.find(Revision.class, query));

		List<Revision> unique = new ArrayList<>();
		int count = 0;
		for(Revision revision : revisions){
			if(!unique.contains(revision))
				unique.add(revision);
		}



		for(Revision revision : revisions) {
			String author = revision.getAuthor();
			if (author == null) {
				author = "<no author>";
			}
			Date day = revision.getDay();


			int size = unique.size();

			Map<Date, Integer> authorCommits = res.get(author);
			if (authorCommits == null) {
				authorCommits = new HashMap<Date, Integer>();
				res.put(author, authorCommits);
			}
			authorCommits.put(day, size);
		}


		/*PreparedStatement ps = null;
		int i = 0;
		try {
			ps = c.prepareStatement(sql);
			if (isDirectory) {
				// dir
				ps.setLong(++i, startRev);
				ps.setLong(++i, endRev);
				ps.setLong(++i, repoId);
				ps.setString(++i, path);
				ps.setString(++i, name);

				// subdir
				ps.setString(++i, path + name + (name.equals("") ? "" : "/")
						+ "%");
			} else {
				ps.setLong(++i, startRev);
				ps.setLong(++i, endRev);
				ps.setLong(++i, repoId);
				ps.setString(++i, path);
				ps.setString(++i, name);
			}

			log.debug(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String author = rs.getString(3);
				if (author == null) {
					author = "<no author>";
				}
				Date day = rs.getDate(2);
				int size = rs.getInt(1);

				Map<Date, Integer> authorCommits = res.get(author);
				if (authorCommits == null) {
					authorCommits = new HashMap<Date, Integer>();
					res.put(author, authorCommits);
				}
				authorCommits.put(day, size);
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/

		return res;
	}

	protected Map<Date, Integer> getHistorySizeForUserDay(long repoId,
			String username) throws Exception {

		Connection conn = null;
		try {
			/*conn = getConnection();*/

			return getHistorySizeForUserDay(repoId, username);

		} finally {
			closeConnection(conn);
		}
	}

	/*protected Map<String, Map<Date, Integer>> getHistorySizeForJIRAFilter(
			long filterId) throws Exception {

		Connection conn = null;
		try {
			*//*conn = getConnection();*//*

			return getHistorySizeForJIRAFilter(filterId);

		} finally {
			closeConnection(conn);
		}
	}*/

	public Map<String, Map<Date, Integer>> getHistorySizeForJIRAFilter(long filterId) throws Exception {

		Map<String, Map<Date, Integer>> res = new HashMap<String, Map<Date, Integer>>();

		List<KeyModel> jiraFilterKeys = DatabaseFunctions.getJIRAFilterIssues(filterId);

		String strMappedProjects = "";
		String strMappedIssues = "";

		for(KeyModel keyModel : jiraFilterKeys){

			strMappedIssues = strMappedIssues + "," + keyModel.getIssue();
			strMappedProjects = strMappedProjects + "," + keyModel.getProject();

		}

		Iterable<String> matchValuesProjects = Splitter.on(',').split(strMappedProjects);
		Iterable<String> matchValuesIssues = Splitter.on(',').split(strMappedIssues);


		String placeholderCommaListProjects = Joiner.on(", ").join(Iterables.transform(matchValuesProjects, Functions.constant("?")));
		String placeholderCommaListIssues = Joiner.on(", ").join(Iterables.transform(matchValuesIssues, Functions.constant("?")));

		Object[] matchValuesArrayProjects = Iterables.toArray(matchValuesProjects, Object.class);
		Object[] matchValuesArrayIssues = Iterables.toArray(matchValuesIssues, Object.class);


	/*	String sql = "select count(distinct r.revision) as size, day, author"
				+ " from REVISIONS as r, JIRA_FILTER(?) as f, KEYS as k"
				+ " where k.project=f.project and k.issue=f.issue and r.repoId=k.repoId and r.revision=k.revision"
				+ " group by day, author";*/

		Query query = Query.select("r.AUTHOR, r,DAY").alias(Revision.class, "r").alias(Key.class, "k")
				.join(Key.class,"r.REPO_ID = k.REPO_ID AND r.REVISION_ID = k.REVISION_ID")
				.where("k.PROJECT IN (" + placeholderCommaListProjects + ") AND k.ISSUE IN (" + placeholderCommaListIssues + ")", matchValuesArrayProjects, matchValuesArrayIssues)
				.group("r.AUTHOR, r,DAY");

		Query countingQuery = Query.select("r.REVISION_ID").distinct().alias(Revision.class, "r").alias(Key.class, "k")
				.join(Key.class,"r.REPO_ID = k.REPO_ID AND r.REVISION_ID = k.REVISION_ID")
				.where("k.PROJECT IN (" + placeholderCommaListProjects + ") AND k.ISSUE IN (" + placeholderCommaListIssues + ")", matchValuesArrayProjects, matchValuesArrayIssues)
				.group("r.AUTHOR, r,DAY");

		List<Revision> revisions = newArrayList(this.ao.find(Revision.class, query));


		for(Revision revision : revisions){
			String author = revision.getAuthor();
			Date day = revision.getDay();
			int size = ao.count(Revision.class,query.where("r.AUTHOR = ? AND r.DAY = ? ", author, day));

			Map<Date, Integer> authorCommits = res.get(author);
			if (authorCommits == null) {
				authorCommits = new HashMap<Date, Integer>();
				res.put(author, authorCommits);
			}

			authorCommits.put(day, size);
		}


		/*PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);

			ps.setLong(1, filterId);

			log.debug(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String author = rs.getString(3);
				Date day = rs.getDate(2);
				int size = rs.getInt(1);

				Map<Date, Integer> authorCommits = res.get(author);
				if (authorCommits == null) {
					authorCommits = new HashMap<Date, Integer>();
					res.put(author, authorCommits);
				}
				authorCommits.put(day, size);
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
*/
		return res;
	}

	protected List<SVNLogEntry> getUserCommitsForJIRAFilter(String author,
			long filterId) throws Exception {

		List<SVNLogEntry> res = new ArrayList<SVNLogEntry>();

		List<KeyModel> jiraFilterKeys = DatabaseFunctions.getJIRAFilterIssues(filterId);

		String strMappedProjects = "";
		String strMappedIssues = "";

		for(KeyModel keyModel : jiraFilterKeys){

			strMappedIssues = strMappedIssues + "," + keyModel.getIssue();
			strMappedProjects = strMappedProjects + "," + keyModel.getProject();

		}

		Iterable<String> matchValuesProjects = Splitter.on(',').split(strMappedProjects);
		Iterable<String> matchValuesIssues = Splitter.on(',').split(strMappedIssues);


		String placeholderCommaListProjects = Joiner.on(", ").join(Iterables.transform(matchValuesProjects, Functions.constant("?")));
		String placeholderCommaListIssues = Joiner.on(", ").join(Iterables.transform(matchValuesIssues, Functions.constant("?")));

		Object[] matchValuesArrayProjects = Iterables.toArray(matchValuesProjects, Object.class);
		Object[] matchValuesArrayIssues = Iterables.toArray(matchValuesIssues, Object.class);

		/*String sql = "select distinct r.repoId, r.revision, r.author, r.timestamp, c.comment "
				+ " from REVISIONS as r, JIRA_FILTER(?) as f, KEYS as k, COMMENTS as c"
				+ " where c.repoId=r.repoId and c.revision=r.revision and k.project=f.project and k.issue=f.issue and r.repoId=k.repoId and r.revision=k.revision"
				+ " and author=? order by r.revision desc";*/

		Query query = Query.select().alias(Revision.class, "r").alias(Comment.class, "c").alias(Key.class, "k")
				.join(Key.class, "r.REPO_ID=k.REPO_ID and r.REVISION_ID=k.REVISION_ID")
				.join(Revision.class,"c.REPO_ID=r.REPO_ID and c.REVISION_ID=r.REVISION_ID")
				.where("k.PROJECT IN (" + placeholderCommaListProjects + ") AND k.ISSUE IN (" + placeholderCommaListIssues + ") AND r.AUTHOR = ? ", matchValuesArrayProjects, matchValuesArrayIssues, author)
				.order("r.REVISION_ID DESC");

		List<Comment> comments = newArrayList(this.ao.find(Comment.class, query));

		SVNLogEntry logEntry;
		for(Comment comment : comments) {
			long revision = comment.getRevision().getRevision();
			Timestamp ts = comment.getRevision().getRTimestamp();
			String message = comment.getComment();
			long repoId = comment.getRepo().getID();

			Map<String, SVNLogEntryPath> changedPaths = RevisionIndexer
					.getLogEntryPathsInTrans(repoId, revision, ao);
			logEntry = new SVNLogEntry(repoId, changedPaths, revision,
					author, ts, message);
			res.add(logEntry);
		}

		/*PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = PluginConnectionPool.getConnection();
			ps = conn.prepareStatement(sql);

			ps.setLong(1, filterId);
			ps.setString(2, author);

			log.debug(sql);
			ResultSet rs = ps.executeQuery();

			SVNLogEntry logEntry;
			while (rs.next()) {
				long revision = rs.getLong("revision");
				Timestamp ts = rs.getRTimestamp("timestamp");
				String message = rs.getString("comment");
				long repoId = rs.getLong("repoId");
				Map<String, SVNLogEntryPath> changedPaths = RevisionIndexer
						.getLogEntryPathsInTrans(conn, repoId, revision);
				logEntry = new SVNLogEntry(repoId, changedPaths, revision,
						author, ts, message);
				res.add(logEntry);
			}
			rs.close();
		} finally {
			PluginConnectionPool.closeStatement(ps);
			PluginConnectionPool.closeConnection(conn);
		}*/
		return res;
	}

	protected List<SVNLogEntry> getDateCommitsForJIRAFilter(String date,
			long filterId) throws Exception {

		List<SVNLogEntry> res = new ArrayList<SVNLogEntry>();

		/*String sql = "select distinct r.repoId, r.revision, r.author, r.timestamp, r.author, c.comment "
				+ " from REVISIONS as r, JIRA_FILTER(?) as f, KEYS as k, COMMENTS as c"
				+ " where c.repoId=r.repoId and c.revision=r.revision and k.project=f.project and k.issue=f.issue and r.repoId=k.repoId and r.revision=k.revision"
				+ " and r.day=? order by r.revision desc";*/

		List<KeyModel> jiraFilterKeys = DatabaseFunctions.getJIRAFilterIssues(filterId);

		String strMappedProjects = "";
		String strMappedIssues = "";

		for(KeyModel keyModel : jiraFilterKeys){

			strMappedIssues = strMappedIssues + "," + keyModel.getIssue();
			strMappedProjects = strMappedProjects + "," + keyModel.getProject();

		}

		Iterable<String> matchValuesProjects = Splitter.on(',').split(strMappedProjects);
		Iterable<String> matchValuesIssues = Splitter.on(',').split(strMappedIssues);


		String placeholderCommaListProjects = Joiner.on(", ").join(Iterables.transform(matchValuesProjects, Functions.constant("?")));
		String placeholderCommaListIssues = Joiner.on(", ").join(Iterables.transform(matchValuesIssues, Functions.constant("?")));

		Object[] matchValuesArrayProjects = Iterables.toArray(matchValuesProjects, Object.class);
		Object[] matchValuesArrayIssues = Iterables.toArray(matchValuesIssues, Object.class);


		Query query = Query.select().alias(Revision.class, "r").alias(Comment.class, "c").alias(Key.class, "k")
				.join(Key.class, "r.REPO_ID=k.REPO_ID and r.REVISION_ID=k.REVISION_ID")
				.join(Revision.class,"c.REPO_ID=r.REPO_ID and c.REVISION_ID=r.REVISION_ID")
				.where("k.PROJECT IN (" + placeholderCommaListProjects + ") AND k.ISSUE IN (" + placeholderCommaListIssues + ") AND r.DAY = ? ", matchValuesArrayProjects, matchValuesArrayIssues, date)
				.order("r.REVISION_ID DESC");

		List<Comment> comments = newArrayList(this.ao.find(Comment.class, query));

		SVNLogEntry logEntry;
		for(Comment comment : comments) {
			long revision = comment.getRevision().getRevision();
			Timestamp ts = comment.getRevision().getRTimestamp();
			String message = comment.getComment();
			long repoId = comment.getRepo().getID();
			String author = comment.getRevision().getAuthor();
			Map<String, SVNLogEntryPath> changedPaths = RevisionIndexer
					.getLogEntryPathsInTrans(repoId, revision, ao);
			logEntry = new SVNLogEntry(repoId, changedPaths, revision,
					author, ts, message);
			res.add(logEntry);
		}


		/*PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = PluginConnectionPool.getConnection();
			ps = conn.prepareStatement(sql);

			ps.setLong(1, filterId);

			Date jDate = JQLSVNDB.getSimpleDateFormat().parse(date);
			ps.setDate(2, new java.sql.Date(jDate.getTime()));

			log.debug(sql);

			ResultSet rs = ps.executeQuery();

			SVNLogEntry logEntry;
			while (rs.next()) {
				long revision = rs.getLong("revision");
				Timestamp ts = rs.getRTimestamp("timestamp");
				String message = rs.getString("comment");
				long repoId = rs.getLong("repoId");
				String author = rs.getString("author");
				Map<String, SVNLogEntryPath> changedPaths = RevisionIndexer
						.getLogEntryPathsInTrans(conn, repoId, revision);
				logEntry = new SVNLogEntry(repoId, changedPaths, revision,
						author, ts, message);
				res.add(logEntry);
			}
			rs.close();
		} finally {
			PluginConnectionPool.closeStatement(ps);
			PluginConnectionPool.closeConnection(conn);
		}*/
		return res;
	}

	private Map<Date, Integer> getHistorySizeForUserDay(Connection c,
			long repoId, String username) throws Exception {

		Map<Date, Integer> res = new HashMap<Date, Integer>();

		/*String sql = "select count(r.revision) as size, day"
				+ " from REVISIONS as r"
				+ " where r.timestamp is not null and r.repoId=? and r.author=?"
				+ " group by day";
*/
		Query query = Query.select("DAY").where("RTIMESTAMP IS NOT NULL AND REPO_ID = ? AND AUTHOR = ?").group("DAY");
		List<Revision> revisions = newArrayList(this.ao.find(Revision.class, query));

		for(Revision revision:revisions) {
			Date day = revision.getDay();
			int size = this.ao.count(Revision.class, query.where("DAY = ? ", day));

			res.put(day, size);
		}


		/*PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sql);

			ps.setLong(1, repoId);
			ps.setString(2, username);

			log.debug(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Date day = rs.getDate(2);
				int size = rs.getInt(1);

				res.put(day, size);
			}
			rs.close();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}*/

		return res;
	}


}
