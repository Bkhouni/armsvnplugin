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

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.utils.Keys;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import org.apache.log4j.LogManager;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;


import java.sql.SQLException;
import java.util.Date;

public class ScheduleMgr {

	private static final org.apache.log4j.Logger log = LogManager.getLogger("atlassian.plugin");

	private static final String TRIGGER_NAME = "svnIndexerTrigger";
	private static final String JOB_NAME = "svnIndexerJob";
	private static final String JOB_ID = "kintosoft.subversion.alm:SchedulerMgr:update";

	private static ScheduleMgr instance;

	private SchedulerService schedulerService;

	private JobDetail job;

	private final ALMMultipleSubversionRepositoryManager almsrm;
	public static ActiveObjects ao;


	private ScheduleMgr(ALMMultipleSubversionRepositoryManager almsrm, SchedulerService schedulerService, ActiveObjects ao) {
		log.info("A scheduler singleton instance has been created");
		instance = this;
		this.almsrm = almsrm;
		this.schedulerService = schedulerService;
		ScheduleMgr.ao = ao;
	}

	public static ScheduleMgr getInstance(ALMMultipleSubversionRepositoryManager almsrm, SchedulerService schedulerService) {
		if (instance == null) {
			new ScheduleMgr(almsrm, schedulerService, ao);

		}
		return instance;

	}

	public int getIntervalFromDB() throws SQLException {
		return Integer.parseInt(PluginConnectionPool
				.getProperty(Keys.db.schedule));
	}

	private void saveIntervalInDB(int seconds) throws SQLException {
		PluginConnectionPool.setProperty(Keys.db.schedule,
				Integer.toString(seconds));
		log.info("the schedulers new interval value has been stored in the database: "
				+ seconds + " secs.");
	}

	public void init() throws SQLException, SchedulerException, SchedulerServiceException {

		schedulerService.registerJobRunner(JobRunnerKey.of("kintosoft.subversion.alm:SchedulerMgr"), new UpdateIndexTask(almsrm));
		int seconds = getIntervalFromDB();

		Schedule schedule = Schedule.forInterval(seconds,new Date());
		JobConfig jobConfig = JobConfig.forJobRunnerKey(JobRunnerKey.of("kintosoft.subversion.alm:SchedulerMgr"))
				.withSchedule(schedule);
//				.withParameters(ImmutableMap.<String, Serializable>of("SUBSCRIPTION_ID", subscriptionId));
		JobId jobId = JobId.of("kintosoft.subversion.alm:SchedulerMgr:update");
		schedulerService.scheduleJob(jobId, jobConfig);

//		job = new JobDetail(JOB_NAME, null, UpdateIndexTask.class);
//		job.setDurability(true);
//		job.setVolatility(true);
//		log.info("The sceduler interval has been fetched from the database: "
//				+ seconds);
//		reschedule(seconds);
	}

	public void reschedule(int seconds) throws SchedulerException, SQLException, SchedulerServiceException {
//		log.info("The Scheduler is going to be  configured to be fired every "
//				+ seconds + " seconds...");
//
//		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
//
//		Trigger trigger = new SimpleTrigger(TRIGGER_NAME, null, new Date(),
//				null, SimpleTrigger.REPEAT_INDEFINITELY, seconds * 1000L);
//		trigger.setVolatility(true);
//		trigger.setJobName(JOB_NAME);
//
//		shutdown();
//
//		scheduler.scheduleJob(job, trigger);
//
//		log.info("...the trigger [" + TRIGGER_NAME
//				+ "] has been rescheduled to fire the job [" + JOB_NAME
//				+ "] every " + seconds + " seconds.");

		Schedule schedule = Schedule.forInterval(seconds,new Date());
		JobConfig jobConfig = JobConfig.forJobRunnerKey(JobRunnerKey.of("kintosoft.subversion.alm:SchedulerMgr"))
				.withSchedule(schedule);
//				.withParameters(ImmutableMap.<String, Serializable>of("SUBSCRIPTION_ID", subscriptionId));
		JobId jobId = JobId.of("kintosoft.subversion.alm:SchedulerMgr:update");
		schedulerService.scheduleJob(jobId, jobConfig);

//		scheduler.start();

		saveIntervalInDB(seconds);
	}

	public Date getLatestFire() throws SchedulerException {
		Trigger trigger = StdSchedulerFactory.getDefaultScheduler().getTrigger(
				TRIGGER_NAME, null);
		if (trigger == null) {
			return null;
		}
		schedulerService.getJobDetails(JobId.of(JOB_ID)).getNextRunTime();


		return new Date();
	}

	public void shutdown() throws SchedulerException {
//		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		if (schedulerService.getJobDetails(JobId.of(JOB_ID)) != null) {
			log.info("Stopping the scheduler...");
			try {
				schedulerService.unscheduleJob(JobId.of(JOB_ID));
				log.info("... the scheduler has been stopped");
			}catch(Exception e){
				log.warn("... the scheduler cannot be stopped");
				e.printStackTrace();
			}

		} else {
			log.warn("No scheduled job found.");
		}
	}
}
