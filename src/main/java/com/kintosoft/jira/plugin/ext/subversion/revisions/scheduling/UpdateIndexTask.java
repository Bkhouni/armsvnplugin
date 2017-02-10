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

package com.kintosoft.jira.plugin.ext.subversion.revisions.scheduling;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class UpdateIndexTask implements JobRunner {

	private final static Logger logger = LoggerFactory
			.getLogger(UpdateIndexTask.class);

	private final ALMMultipleSubversionRepositoryManager almsrm;

	public UpdateIndexTask(ALMMultipleSubversionRepositoryManager almsrm){
		this.almsrm = almsrm;
	}
/*	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {

			ALMMultipleSubversionRepositoryManagerImpl.updateIndexStatic();

		} catch (Exception e) {
			logger.error("Error indexing changes: " + e);
		}
	}*/

	@Nullable
	@Override
	public JobRunnerResponse runJob(JobRunnerRequest jobRunnerRequest) {
		try {

			ALMMultipleSubversionRepositoryManagerImpl.updateIndexStatic();

		} catch (Exception e) {
			logger.error("Error indexing changes: " + e);
			return JobRunnerResponse.aborted("Task aborted due to the following error : " + e);
		}
		return JobRunnerResponse.success();
	}
}
