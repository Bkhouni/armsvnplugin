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

package com.kintosoft.jira.reports;

import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.kintosoft.jira.utils.Utils;

import java.util.Map;

public class SVNFilterReport extends AbstractReport {

	final private WebResourceManager wrm;

	public SVNFilterReport(WebResourceManager wrm) {
		this.wrm = wrm;
	}

	public String generateReportHtml(ProjectActionSupport projectActionSupport,
			Map map) throws Exception {

		wrm.requireResource("com.kintosoft.jira.subversion-plus:subversion-alm-resource-js");

		String filterId = (String) map.get("filterId");

		map.put("filterName", Utils.getFilerName(filterId));

		return this.descriptor.getHtml("view", map);
	}

	@Override
	public boolean showReport() {
		return true;
	}

	public void validate(ProjectActionSupport action, Map map) {
		String filterId = (String) map.get("filterId");

		if (filterId == null) {
			action.addError("filterId", action.getText("filterId is null"));
		} else if (filterId.equals("")) {
			action.addError("filterId", action.getText("Invalid filter value"));
		}

		super.validate(action, map);
	}
}