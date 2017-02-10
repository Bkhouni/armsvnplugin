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
import com.kintosoft.svnwebclient.graph.model.Graph;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.polarion.svnwebclient.web.SystemInitializing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;

@RemoteProxy
public class CommitGraphService {

	private final ActiveObjects ao;

	private static Logger log = LoggerFactory
			.getLogger(CommitGraphService.class);

	public CommitGraphService(ActiveObjects ao){
		this.ao = ao;
	}

	@RemoteMethod
	public Object getGraph(String repoId, String item, String pegRevision,
			boolean isDir, boolean loadSegmentActions) throws Exception {

		checkPrivileges(Long.parseLong(repoId));

		if (!item.startsWith("/")) {
			item = "/" + item;
		}
		Graph graph = null;
		try {
			DBGraph dbGraph = new DBGraph(ao);

			graph = dbGraph.buildGraph(Long.parseLong(repoId), item,
					Long.parseLong(pegRevision), isDir, false, false, true,
					loadSegmentActions);

			return graph;
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	@RemoteMethod
	public SegmentCommitsRequest getSegmentCommits(long repoId, String path,
			String name, String startRev, String endRev, boolean isDir)
			throws Exception {

		checkPrivileges(repoId);

		SegmentCommitsRequest response = new SegmentCommitsRequest();
		response.endRev = Long.parseLong(endRev);
		response.isDir = isDir;
		response.name = name;
		response.path = path;
		response.repoId = repoId;
		response.startRev = Long.parseLong(startRev);

		try {
			DBGraph dbGraph = new DBGraph(ao);

			response.actions = dbGraph.revisionsInRangePopulated(
					response.repoId, response.path, response.name,
					response.startRev, response.endRev, response.isDir);

			return response;

		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}

	protected static void checkPrivileges(Long repoId) throws Exception {
		if (repoId == null) {
			throw new Exception("The client repoId is null");
		}

		HttpSession session = WebContextFactory.get().getSession();
		if (session == null) {
			throw new Exception("No session available");
		}
		Long sessionRepoId = (Long) session
				.getAttribute(SystemInitializing.REPOID);
		if (sessionRepoId == null) {
			throw new Exception("The session repoId is null");
		}
		if (!repoId.equals(sessionRepoId)) {
			throw new Exception("The client does not match the session repoId");
		}
	}
}
