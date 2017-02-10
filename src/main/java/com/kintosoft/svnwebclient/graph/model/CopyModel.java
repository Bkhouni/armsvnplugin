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

@DataTransferObject(converter = ObjectConverter.class)
public class CopyModel implements Comparable<CopyModel> {

	@RemoteProperty
	public ItemModel fromItem;

	@RemoteProperty
	public RevisionModel fromRevision;

	@RemoteProperty
	public ItemModel toItem;

	@RemoteProperty
	public RevisionModel toRevision;

	/*
	 * protected Copy(String fromParent, String fromChild, String fromRev,
	 * String toParent, String toChild, String toRev) { this(new
	 * Item(fromParent, fromChild), new Revision(fromRev), new Item( toParent,
	 * toChild), new Revision(toRev)); }
	 */
	protected CopyModel(ItemModel fromItem, RevisionModel fromRevision, ItemModel toiItem,
						RevisionModel toRevision) {
		this.fromItem = fromItem;
		this.fromRevision = fromRevision;
		this.toItem = toiItem;
		this.toRevision = toRevision;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CopyModel)) {
			return false;
		}
		CopyModel copy = (CopyModel) obj;
		return copy.fromItem.equals(this.fromItem)
				&& copy.fromRevision.equals(this.fromRevision)
				&& copy.toItem.equals(this.toItem)
				&& copy.toRevision.equals(this.toRevision);
	}

	@Override
	public String toString() {
		return fromItem + "@" + fromRevision + " -> " + toItem + "@"
				+ toRevision;
	}

	public int compareTo(CopyModel target) {
		if (this.fromRevision.equals(target.fromRevision)) {
			return this.toRevision.compareTo(target.toRevision);
		}
		return this.fromRevision.compareTo(target.fromRevision);
	}
}
