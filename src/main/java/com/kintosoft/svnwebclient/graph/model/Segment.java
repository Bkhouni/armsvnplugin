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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@DataTransferObject(converter = ObjectConverter.class)
public class Segment {

	@RemoteProperty
	public ItemModel item;

	@RemoteProperty
	public long start;

	@RemoteProperty
	public long end;

	@RemoteProperty
	public boolean meta_deleted;

	@RemoteProperty
	public ItemModel meta_renamed;

	@RemoteProperty
	public List<ActionModel> actions;

	public Segment(ItemModel item, long start, long end) {
		this.item = item;
		this.start = start;
		this.end = end;
		this.actions = new ArrayList<ActionModel>();
	}

	@Override
	public String toString() {
		return item.toString() + "[" + start + "," + end + "]";
	}

	public boolean conflicts(Segment target) {
		if (target.item.equals(this.item)) {
			if (between(target.start) || between(target.end)) {
				return true;
			}
		}
		return false;
	}

	protected void addAction(ActionModel act) throws Exception {
		actions.add(act);
	}

	public boolean between(long number) {
		return number >= start && number <= end;
	}

	// must be sorted
	// the segment is always sorted during the build process
	// but can be un-sorted because the non-strict option adding G actions
	// so this must be called BEFORE managing the strict option and sorted AFTER
	public ActionModel getClosestAction(long rev) {
		ActionModel prev = null;
		for (ActionModel act : actions) {
			if (act.revision.number < rev) {
				prev = act;
				continue;
			}
			if (act.revision.number == rev) {
				return act;
			}
			break;
		}
		return prev;
	}

	public void sort() {
		Collections.sort(actions, new Comparator<ActionModel>() {
			@Override
			public int compare(ActionModel a1, ActionModel a2) {
				return (int) (a1.revision.number - a2.revision.number);
			}
		});
	}
}
