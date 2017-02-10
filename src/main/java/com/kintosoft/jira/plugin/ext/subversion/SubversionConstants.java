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

package com.kintosoft.jira.plugin.ext.subversion;

/**
 * Letters indicating changes in the Subversion repository. Defined in <a
 * href="http://svnbook.red-bean.com/en/1.1/ch03s05.html#svn-ch-3-sect-5.1">the
 * Subversion book</a>.
 */
public interface SubversionConstants {
	char MODIFICATION = 'M';
	char ADDED = 'A';
	char DELETED = 'D';
	char REPLACED = 'R';
}
