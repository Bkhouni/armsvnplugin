package com.kintosoft.jira.plugin.ext.subversion;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.scheduler.SchedulerService;
import com.kintosoft.jira.plugin.ext.subversion.revisions.scheduling.ScheduleMgr;
import com.kintosoft.svnwebclient.db.PluginConnectionPool;
import com.kintosoft.svnwebclient.indexing.RevisionIndexer;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import com.kintosoft.svnwebclient.utils.SVNLogEntry;
import com.opensymphony.module.propertyset.PropertySet;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.data.javasvn.DataProvider;
import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This is a wrapper class for many SubversionManagers. Configured via
 * {@link SvnPropertiesLoader#PROPERTIES_FILE_NAME}
 * 
 * @author Dylan Etkin
 * @see {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
 */



public class ALMMultipleSubversionRepositoryManagerImpl implements
		ALMMultipleSubversionRepositoryManager {

	public static org.slf4j.Logger log = LoggerFactory
			.getLogger(ALMMultipleSubversionRepositoryManagerImpl.class);

	private static final String REVISIONS_INDEX_DIRECTORY = "kintosoft_subversion";

	public static final String APP_PROPERTY_PREFIX = "jira.plugins.subversion";

	public static final String REPO_PROPERTY = "jira.plugins.subversion.repo";

	public static final String LAST_REPO_ID = "last.repo.id";

	public static final long FIRST_REPO_ID = 1;

	private PropertySet pluginProperties;

	private RevisionIndexer revisionIndexer;

	private Map<Long, SubversionManager> managerMap = new HashMap<Long, SubversionManager>();

	private final JiraPropertySetFactory jiraPropertySetFactory;

	private long lastRepoId;

	private static ALMMultipleSubversionRepositoryManagerImpl me;

	private boolean isInitialized = false;

	private static String clientId;

	private static String INDEX_PATH;



	private static ActiveObjects ao;


	private final SchedulerService schedulerService;

	private final ChangeHistoryManager changeHistoryManager;
	private final IndexPathManager indexPathManager;
	private final VersionManager versionManager;
	private final IssueManager issueManager;
	private final PermissionManager permissionManager;


	public ALMMultipleSubversionRepositoryManagerImpl(
			JiraPropertySetFactory jiraPropertySetFactory,SchedulerService schedulerService, ActiveObjects ao) throws Exception

	{
		this.changeHistoryManager = ComponentAccessor.getChangeHistoryManager();

		me = this;
		this.jiraPropertySetFactory = jiraPropertySetFactory;
		this.indexPathManager = ComponentAccessor.getIndexPathManager();
		this.versionManager = ComponentAccessor.getVersionManager();
		this.issueManager = ComponentAccessor.getIssueManager();
		this.permissionManager = ComponentAccessor.getPermissionManager();
		this.schedulerService = schedulerService;
		this.ao = ao;
	}


	@PostConstruct
	public void start() {
		try {
			revisionIndexer = new RevisionIndexer(me, versionManager,
					issueManager, permissionManager, changeHistoryManager, ao);

			System.setProperty("svnkit.library.gnome-keyring.enabled", "false");
			System.setProperty("svnkit.http.methods",
					"Basic,Digest,Negotiate,NTLM");
			Field fieldSysPath = ClassLoader.class
					.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);

//			clientId = ComponentManager.getComponentInstanceOfType(
//					JiraLicenseService.class).getServerId();

			// Initialize the SVN tools
			DAVRepositoryFactory.setup();
			SVNRepositoryFactoryImpl.setup();
			FSRepositoryFactory.setup();

			PluginConnectionPool.createSchema();

			revisionIndexer = new RevisionIndexer(me, versionManager,
					issueManager, permissionManager, changeHistoryManager, ao);


			ScheduleMgr.getInstance(this, schedulerService).init();

		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		me.init();
	}

	public static String getIndexPath() {
		return INDEX_PATH;
	}

	public static void reset() {
		me.isInitialized = false;
		me.init();
	}

	synchronized private void init() {


		if (isInitialized) {
			return;
		}
		isInitialized = true;
		managerMap = loadManagersFromSWCProperties();



		Iterator it = managerMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			log.info(pair.getKey() + "  =  " + pair.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}

	}

	/**
	 * Loads a {@link java.util.Map} of
	 * {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager} IDs to
	 * the {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}.
	 * The repositories are loaded from persistent storage. If they couldn't be
	 * found there, we will try to look for them in the plugin's configuration
	 * file.
	 * 
	 * @return A map of
	 *         {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
	 *         IDs to the
	 *         {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
	 *         .
	 * @throws InfrastructureException
	 *             Thrown if there's a problem loading repositories
	 *             configuration from the plugin's configuration file.
	 */
	public static Map<Long, SubversionManager> loadSvnManagers() {

		Map<Long, SubversionManager> managers = me
				.loadManagersFromJiraProperties();

		if (managers.isEmpty()) {
			log.info("Could not find any subversion repositories configured, trying to load from "
					+ SvnPropertiesLoader.PROPERTIES_FILE_NAME);
			managers = me.loadFromProperties();
		}

		return managers;
	}

	Map<Long, SubversionManager> loadManagersFromSWCProperties() {
		Map<Long, SubversionManager> repos = new LinkedHashMap<Long, SubversionManager>();
		try {
			SWCUtils.setAO(ao,this);
			List<ConfigurationProvider> swcRepos = SWCUtils.getRepositories();
			for (ConfigurationProvider swcRepo : swcRepos) {
				log.warn("--------------- Current repo loaded : " + swcRepo.getRepoId());
				SubversionManager manager = new SubversionManagerImpl(ao,
						revisionIndexer, swcRepo.getRepoId(),
						SWCUtils.swc2jira(swcRepo));
				repos.put(swcRepo.getRepoId(), manager);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return repos;
	}

	/**
	 * The Subversion configuration properties are stored in the application
	 * properties. It's not the best place to store collections of information,
	 * like multiple repositories, but it will work. Keys for the properties
	 * look like:
	 * <p/>
	 * <tt>jira.plugins.subversion.&lt;repoId&gt;;&lt;property name&gt;</tt>
	 * <p/>
	 * Using this scheme we can get all the properties and put them into buckets
	 * corresponding to the <tt>repoId</tt>. Then when we have all the
	 * properties we can go about building the
	 * {@link com.kintosoft.jira.plugin.ext.subversion.SubversionProperties}
	 * objects and creating our
	 * {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}s.
	 * 
	 * @return A {@link java.util.Map} of
	 *         {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
	 *         IDs to the
	 *         {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
	 *         . loaded from JIRA's application properties.
	 */
	private Map<Long, SubversionManager> loadManagersFromJiraProperties() {

		pluginProperties = jiraPropertySetFactory
				.buildCachingDefaultPropertySet(APP_PROPERTY_PREFIX);

		lastRepoId = pluginProperties.getLong(LAST_REPO_ID);

		// create the SubversionManagers
		Map<Long, SubversionManager> managers = new LinkedHashMap<Long, SubversionManager>();
		for (long i = FIRST_REPO_ID; i <= lastRepoId; i++) {
			SubversionManager mgr = createManagerFromPropertySet(i,
					jiraPropertySetFactory.buildCachingPropertySet(
							REPO_PROPERTY, i, true));
			if (mgr != null)
				managers.put(i, mgr);
		}
		return managers;
	}

	SubversionManager createManagerFromPropertySet(long index,
			PropertySet properties) {
		try {
			if (properties.getKeys().isEmpty())
				return null;

			return new SubversionManagerImpl(ao, revisionIndexer, index, properties);
		} catch (IllegalArgumentException e) {
			log.error(
					"Error creating SubversionManager "
							+ index
							+ ". Probably was missing a required field (e.g., repository name or root). Skipping it.",
					e);
			return null;
		}
	}

	/**
	 * Loads a {@link java.util.Map} of
	 * {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager} IDs to
	 * the {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}.
	 * The configuration is loaded from the plugin's configuration file.
	 * 
	 * @return A {@link java.util.Map} of
	 *         {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
	 *         IDs to the
	 *         {@link com.kintosoft.jira.plugin.ext.subversion.SubversionManager}
	 *         .
	 * @throws InfrastructureException
	 *             Thrown if there's a problem loading repositories
	 *             configuration from the plugin's configuration file.
	 */
	Map<Long, SubversionManager> loadFromProperties() {
		Map<Long, SubversionManager> managers = new HashMap<Long, SubversionManager>();

		try {
			List<SubversionProperties> propertiesSet = SvnPropertiesLoader
					.getSVNProperties();
			for (SubversionProperties svnProperties : propertiesSet) {
				SubversionManager mgr = createRepositoryWithoutRegister(svnProperties);
				managers.put(mgr.getId(), mgr);
			}
		} catch (InfrastructureException ie) {
			log.warn("There's a problem adding a subversion manager.", ie);
		}

		return managers;
	}

	public SubversionManager createRepositoryWithoutRegister(
			SvnProperties properties) {
		long repoId;
		synchronized (this) {
			repoId = ++lastRepoId;
			pluginProperties.setLong(LAST_REPO_ID, lastRepoId);
		}

		PropertySet set = jiraPropertySetFactory.buildCachingPropertySet(
				REPO_PROPERTY, repoId, true);
		SubversionManager subversionManager = new SubversionManagerImpl(ao,
				revisionIndexer, repoId, SvnProperties.Util.fillPropertySet(
						properties, set));

		return subversionManager;
	}

	public SubversionManager createRepository(SvnProperties properties) {

		SubversionManager subversionManager = createRepositoryWithoutRegister(properties);

		managerMap.put(subversionManager.getId(), subversionManager);
		if (isIndexingRevisions()) {
			revisionIndexer.addRepository(subversionManager);
		}

		return subversionManager;
	}

	public SubversionManager createRepository(long repoId,
			PropertySet properties) {
		SubversionManager subversionManager = new SubversionManagerImpl(ao,
				revisionIndexer, repoId, properties);

		managerMap.put(subversionManager.getId(), subversionManager);
		log.debug("Created a new SubversionManagerImpl instance and added to the manager map with ID="
				+ subversionManager.getId());
		if (isIndexingRevisions()) {
			revisionIndexer.addRepository(subversionManager);
		} else {
			log.debug("The revisionIndexer is NULL!");
		}

		return subversionManager;
	}

	public SubversionManager updateRepository(long repoId,
			SvnProperties properties) {
		SubversionManager subversionManager = getRepository(repoId);
		subversionManager.update(properties);
		return subversionManager;
	}

	public SubversionManager updateRepository(long repoId,
			PropertySet properties) {
		SubversionManager subversionManager = getRepository(repoId);
		subversionManager.update(properties);
		return subversionManager;
	}

	public void removeRepository(long repoId) {

		init();
		SubversionManager original = managerMap.get(repoId);
		if (original == null) {
			return;
		}
		try {
			managerMap.remove(repoId);

			// Would like to just call remove() but this version doesn't appear
			// to have that, remove all of it's properties instead
			/*for (String key : new ArrayList<String>(original.getProperties()
					.getKeys()))
				original.getProperties().remove(key);*/

			if (revisionIndexer != null)
				revisionIndexer.removeEntries(original);
		} catch (Exception e) {
			throw new InfrastructureException(
					"Could not remove repository index", e);
		}
	}

	public boolean isIndexingRevisions() {
		return revisionIndexer != null;
	}

	public Collection<SubversionManager> getRepositoryList() {
		init();
		return managerMap.values();
	}

	public SubversionManager getRepository(long id) {
		init();
		return managerMap.get(id);
	}

	@Override
	public void updateIndex() throws Exception {
		if (revisionIndexer != null) {
			revisionIndexer.updateIndex();
		} else {
			log.warn("The subversion indexer is not ready: updateIndex()");
		}
	}

	@Override
	public List<SVNLogEntry> getLogEntriesByRepository(Issue issue,
			int startIndex, int pageSize, boolean ascending) throws Exception {
		if (revisionIndexer != null) {

			List<String> issues = new ArrayList<String>();
			Collection<String> previousIssueKeys = changeHistoryManager
					.getPreviousIssueKeys(issue.getId());
			issues.addAll(previousIssueKeys);
			issues.add(issue.getKey());

			return revisionIndexer.getLogEntriesByRepository(issues,
					startIndex, pageSize, ascending);
		} else {
			log.warn("The subversion indexer is not ready: getLogEntriesByRepository()");
			return null;
		}
	}

	@Override
	public List<SVNLogEntry> getLogEntriesByProject(String projectKey,
			ApplicationUser user, int startIndex, int pageSize)
			throws Exception {
		if (revisionIndexer != null) {
			return revisionIndexer.getLogEntriesByProject(projectKey, user,
					startIndex, pageSize);
		} else {
			log.warn("The subversion indexer is not ready: getLogEntriesByProject()");
			return null;
		}
	}

	@Override
	public List<SVNLogEntry> getLogEntriesByVersion(Version version,
			ApplicationUser user, int startIndex, int pageSize)
			throws Exception {
		if (revisionIndexer != null) {
			return revisionIndexer.getLogEntriesByVersion(version, user,
					startIndex, pageSize);
		} else {
			log.warn("The subversion indexer is not ready: getLogEntriesByVersion()");
			return null;
		}
	}

	@Override
	public RevisionIndexer getIndexer() {
		return revisionIndexer;
	}

	@PreDestroy
	public void terminate() {
		try {
			ScheduleMgr.getInstance(this, schedulerService).shutdown();
		} catch (SchedulerException e) {
			log.warn(e.getMessage());
		} finally {
			try {
				DataProvider.terminate();
			} catch (Exception e) {
				log.warn(e.getMessage());
			} finally {
				try {
					revisionIndexer.terminate();
					log.info("Active threads after termination:"
							+ revisionIndexer.getCurrentActiveThreads());
				} catch (Exception e) {
					log.warn(e.getMessage());
				} /*finally {
					try {
						PluginConnectionPool.getInstance().shutdown();
					} catch (Exception e) {
						log.warn(e.getMessage());
					}
				}*/
			}
		}
	}

	public static ALMMultipleSubversionRepositoryManagerImpl getInstance() {
		return me;
	}

	public static void updateIndexStatic() throws Exception {
		me.updateIndex();
	}

	public static String validateLicense() {
		return null;
	}
}
