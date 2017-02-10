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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DataTransferObject(converter = ObjectConverter.class)
public class Graph {

	private static Logger log = LoggerFactory.getLogger(Graph.class);

	@RemoteProperty
	public Map<String, RevisionModel> revisions = new HashMap<String, RevisionModel>();

	@RemoteProperty
	public Map<String, ItemModel> items = new HashMap<String, ItemModel>();

	@RemoteProperty
	public List<CopyModel> copies = new ArrayList<CopyModel>();

	@RemoteProperty
	public Map<String, ActionModel> actions = new HashMap<String, ActionModel>();

	@RemoteProperty
	public String tagsName;

	public RevisionModel getRevision(String number) {
		return getRevision(Long.parseLong(number));
	}

	protected RevisionModel getRevision(Long number) {
		String key = Long.toString(number);
		RevisionModel rev = revisions.get(key);
		if (rev == null) {
			rev = new RevisionModel(number);
		}
		revisions.put(key, rev);
		return rev;
	}

	public ItemModel getItem(String path, String name) {
		ItemModel item = items.get(path + name);
		if (item != null) {
			return item;
		}
		item = new ItemModel(path, name);
		items.put(item.key(), item);
		return item;
	}

	public Segment getSegment(ItemModel item, long start, long end) throws Exception {
		Segment segment = item.getSegment(start, end);
		if (segment == null) {
			segment = new Segment(item, start, end);
			item.addSegment(segment);
		}
		return segment;
	}

	public ActionModel action(String id, Long revision, Segment segment)
			throws Exception {
		RevisionModel rev = getRevision(revision);
		return action(id, rev, segment);
	}

	public ActionModel action(String id, RevisionModel rev, Segment segment)
			throws Exception {

		ItemModel item = segment.item;

		String actionKey = ActionModel.getActionKey(item.parent, item.child,
				rev.number);

		ActionModel newAct = actions.get(actionKey);

		if (newAct == null) {
			newAct = new ActionModel(id, rev, segment);
			actions.put(actionKey, newAct);
			segment.addAction(newAct);
			rev.addAction(newAct);
		}

		return newAct;
	}

	public boolean copy(String fromPath, String fromName, long fromRev,
						String toPath, String toName, long toRev) throws Exception {

		CopyModel newCopy = new CopyModel(getItem(fromPath, fromName),
				getRevision(fromRev), getItem(toPath, toName),
				getRevision(toRev));
		/*
		 * if (copyExists(newCopy)) { return false; }
		 */
		copies.add(newCopy);
		return true;
	}

	public boolean revisionExists(long number) {

		return revisions.containsKey(number);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (RevisionModel rev : revisions.values()) {
			if (first) {
				first = false;
			} else {
				sb.append(" : ");
			}
			sb.append(rev);
		}
		sb.append("\n");
		sb.append("----------------------------").append("\n");
		for (Map.Entry<String, ItemModel> e : items.entrySet()) {
			String path = e.getKey();
			ItemModel item = e.getValue();
			sb.append(path).append(": ");
			first = true;
			for (Segment seg : item.segments.values()) {
				for (ActionModel act : seg.actions) {
					if (first) {
						first = false;
					} else {
						sb.append("->");
					}
					sb.append("[").append(act.id).append(":")
							.append(act.revision).append("]");
				}
				sb.append("\n");
			}
		}
		sb.append("----------------------------").append("\n");
		for (CopyModel copy : copies) {
			sb.append(copy).append("\n");
		}
		sb.append("\n");
		sb.append("----------------------------").append("\n");

		return sb.toString();
	}

	public ActionModel getFooAction(String id, long rev) {
		return new ActionModel(id, new RevisionModel(rev), null);
	}

}