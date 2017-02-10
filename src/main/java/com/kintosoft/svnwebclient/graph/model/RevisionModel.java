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

import com.kintosoft.svnwebclient.graph.model.jira.Issue;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.convert.ObjectConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DataTransferObject(converter = ObjectConverter.class)
public class RevisionModel implements Comparable<RevisionModel> {

	@RemoteProperty
	public Long number;

	@RemoteProperty
	public String author;

	@RemoteProperty
	public Date date;

	@RemoteProperty
	public String comment;

	@RemoteProperty
	public List<ActionModel> actions = new ArrayList<ActionModel>();

	@RemoteProperty
	public List<Issue> tickets = new ArrayList<Issue>();

	protected RevisionModel(String number) {
		this(Long.parseLong(number));
	}

	public RevisionModel(Long number) {
		this.number = number;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RevisionModel)) {
			return false;
		}
		RevisionModel rev = (RevisionModel) obj;
		return rev.number.longValue() == this.number.longValue();
	}

	@Override
	public String toString() {
		return Long.toString(number);
	}

	protected boolean addAction(ActionModel act) throws Exception {
		for (ActionModel x : actions) {
			if (x.equals(act)) {
				return true;
			}
			if (!x.revision.equals(this)) {
				throw new Exception("Action doesn't match revision:" + this
						+ ":" + act);
			}
		}
		actions.add(act);
		return false;
	}

	public void removeAction(ActionModel act) throws Exception {
		if (!actions.remove(act)) {
			throw new Exception("Action doesn't exists in revison: " + act);
		}
	}

	public int compareTo(RevisionModel target) {
		return (int) (this.number - target.number);
	}
}
