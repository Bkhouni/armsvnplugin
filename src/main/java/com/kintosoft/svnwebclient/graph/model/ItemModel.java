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

import java.util.HashMap;
import java.util.Map;

@DataTransferObject(converter = ObjectConverter.class)
public class ItemModel {

	@RemoteProperty
	public String parent;

	@RemoteProperty
	public String child;

	@RemoteProperty
	public Map<String, Segment> segments = new HashMap<String, Segment>();

	@RemoteProperty
	public boolean meta_tag;

	protected ItemModel(String parent, String child) {
		this.parent = parent;
		this.child = child;
	}

	@Override
	public String toString() {
		return key();
	}

	@Override
	public boolean equals(Object x) {
		if (!(x instanceof ItemModel)) {
			return false;
		}
		ItemModel item = (ItemModel) x;
		return item.parent.equals(this.parent) && item.child.equals(this.child);
	}

	private String getKeyFromSegment(Segment segment) {
		return getKeyFromSegment(segment.start, segment.end);
	}

	private String getKeyFromSegment(long start, long end) {
		return start + ":" + end;
	}

	Segment getSegment(long start, long end) {
		return segments.get(getKeyFromSegment(start, end));
	}

	void addSegment(Segment segment) throws Exception {
		for (Segment s : segments.values()) {
			if (s.conflicts(segment)) {
				throw new Exception("Segment conflict between " + segment
						+ " and " + s);
			}
		}
		segments.put(getKeyFromSegment(segment), segment);
	}

	public boolean segmentExists(long rev) {
		for (Segment segment : segments.values()) {
			if (segment.between(rev)) {
				return true;
			}
		}
		return false;
	}

	public String key() {
		return parent + child;
	}

	public Segment getSegment(long rev) {
		for (Segment seg : segments.values()) {
			if (seg.between(rev)) {
				return seg;
			}
		}
		return null;
	}
}
