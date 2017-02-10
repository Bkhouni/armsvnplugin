package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.ao.RepoConfig;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IRepositoryConfigurationManager;
import net.java.ao.Query;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 14/09/2016.
 */

@Transactional
public class RepositoryConfigurationManager implements IRepositoryConfigurationManager {

    private final ActiveObjects ao;

    public RepositoryConfigurationManager(ActiveObjects ao){
        this.ao = ao;
    }

    @Override
    public RepoConfig getRepositoryConfigurationById(Integer id) {
        return null;
    }

    @Override
    public RepoConfig addRepositoryConfiguration(long repoId, String key, String value) {
        RepoConfig repoConfig = null;
        Repository repository = this.ao.get(Repository.class, (int)repoId);

        try{
            repoConfig = this.ao.create(RepoConfig.class);
            repoConfig.setValue(value);
            repoConfig.setKey(key);
            repoConfig.setRepo(repository);

            repoConfig.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return repoConfig;
    }

    @Override
    public RepoConfig updateRepositoryConfigurationValue(long repoId, String key, String value) {

        List<RepoConfig> repoConfigList = newArrayList(this.ao.find(RepoConfig.class, Query.select().where("REPO_ID = ? AND KEY = ?", repoId, key)));
        RepoConfig repoConfig = null;
        if(repoConfigList.size() > 0){
            repoConfig = repoConfigList.get(0);
            repoConfig.setValue(value);
            repoConfig.save();
        }


        return repoConfig;
    }

    @Override
    public List<RepoConfig> getRepositoryConfigurationByRepoId(int repoId) {
        Query query = Query.select().where("REPO_ID = ? ", repoId);
        List<RepoConfig> repoConfigList = newArrayList(this.ao.find(RepoConfig.class, query));

        if(repoConfigList.size() > 0){
            return repoConfigList;
        }
        return null;
    }
}
