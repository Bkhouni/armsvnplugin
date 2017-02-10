package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.RepoConfig;

import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IRepositoryConfigurationManager {
    RepoConfig getRepositoryConfigurationById(Integer id);
    RepoConfig addRepositoryConfiguration(long repoId, String name, String value);
    RepoConfig updateRepositoryConfigurationValue(long repoId, String key, String value);
    List<RepoConfig> getRepositoryConfigurationByRepoId(int repoId);
}
