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

package com.kintosoft.jira.utils;

public class Keys {
	public static class db {
		public final static String schedule = "SCHEDULE";
		public final static String accepted = "ACCEPTED";
		public final static String poolsize = "DB_CONNECTION_POOL_SIZE";
		public final static String shareconnections = "DB_SHARE_CONNECTIONS";
		public final static String compactonclose = "DB_COMPACT_ON_CLOSE";
		public final static String requiresession = "DB_REQUIRE_TRACKER_SESSION";
		public final static String svntimeoutconnection = "SVN_CONNECTION_TIMEOUT";
		public final static String svntimeoutread = "SVN_READ_TIMEOUT";
		public final static String maxindexingthreads = "MAX_INDEXING_THREADS";
	}
}
