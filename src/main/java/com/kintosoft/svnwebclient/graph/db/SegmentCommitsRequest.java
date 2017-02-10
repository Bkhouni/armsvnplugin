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

import com.kintosoft.svnwebclient.graph.model.ActionModel;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.convert.ObjectConverter;

import java.util.List;

@DataTransferObject(converter = ObjectConverter.class)
public class SegmentCommitsRequest {

	@RemoteProperty
	public long repoId;

	@RemoteProperty
	public String path;

	@RemoteProperty
	public String name;

	@RemoteProperty
	public long startRev;

	@RemoteProperty
	public long endRev;

	@RemoteProperty
	public boolean isDir;

	@RemoteProperty
	public List<ActionModel> actions;
}
