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

package com.kintosoft.svnwebclient.jira.public_interface;

import java.sql.Connection;
import java.sql.SQLException;

public class SWCPublicInterfaceImpl implements SWCPublicInterface {

	@Override
	public Connection getConnection() throws SQLException {
		/*if (!PluginConnectionPool.getInstance().getShareConnections()) {
			throw new SQLException("Sharing JDBC connections is not enabled");
		}*/
		return null;
	}

}
