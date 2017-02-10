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

package com.kintosoft.svnwebclient.graph.model;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.convert.ObjectConverter;

import java.util.ArrayList;
import java.util.List;

@DataTransferObject(converter = ObjectConverter.class)
public class ActionModel implements Comparable<ActionModel> {

	@RemoteProperty
	public String id;

	@RemoteProperty
	public RevisionModel revision;

	@RemoteProperty
	public Segment segment;

	@RemoteProperty
	public ActionModel parent;

	@RemoteProperty
	public List<ActionModel> children = new ArrayList<ActionModel>();

	@Override
	public String toString() {
		return id + " " + key();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ActionModel)) {
			return false;
		}
		ActionModel act = (ActionModel) obj;
		return act.id.equals(this.id) && act.revision.equals(this.revision)
				&& act.segment.item.equals(this.segment.item);
	}

	protected ActionModel(String id, RevisionModel revision, Segment segment) {
		this.id = id;
		this.revision = revision;
		this.segment = segment;
	}

	public int compareTo(ActionModel target) {
		if (target.revision.equals(this.revision)) {
			if (target.id.equals("G")) {
				return -1;
			} else if (this.id.equals("G")) {
				return 1;
			} else {
				return 0;
			}
		}
		return this.revision.compareTo(target.revision);
	}

	public String key() {
		return getActionKey(segment.item.parent, segment.item.child,
				revision.number);
	}

	public static String getActionKey(String parent, String child, long revision) {
		return parent + child + "@" + revision;
	}

}
