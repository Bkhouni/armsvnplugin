package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.Repository;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IRepositoryManager {
    Repository deleteRepository(long repoId);
    Repository addRepository(String url, String name);
    Repository getRepositoryByName(String name);
    Repository getRepositoryByURL(String url);
    void updateRepository(String url, String name, long repoId);

}
