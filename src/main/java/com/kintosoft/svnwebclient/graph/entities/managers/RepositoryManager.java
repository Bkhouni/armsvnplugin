package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.kintosoft.svnwebclient.graph.entities.ao.*;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IRepositoryManager;
import net.java.ao.Query;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 14/09/2016.
 */
public class RepositoryManager implements IRepositoryManager {

    ActiveObjects ao;

    public RepositoryManager(ActiveObjects ao){
        this.ao = ao;
    }



    @Override
    public Repository deleteRepository(long repoId) {

        Repository repo = this.ao.get(Repository.class, (int)repoId);

        for(Comment comment : repo.getComments())
            this.ao.delete(comment);

        for(Copy copy : repo.getCopies())
            this.ao.delete(copy);

        for(Action action : repo.getActions())
            this.ao.delete(action);

        for(Item item : repo.getItems())
            this.ao.delete(item);

        for(RepoConfig repoConfig : repo.getRepositoryConfigurations())
            this.ao.delete(repoConfig);

        for(Key key : repo.getKeys())
            this.ao.delete(key);

        for(Revision revision : repo.getRevisions())
            this.ao.delete(revision);


        this.ao.delete(repo);
        this.ao.flushAll();

        return repo;
    }


    @Override
    public Repository addRepository(String url, String name) {
        Repository repository = null;
        try{
            repository = this.ao.create(Repository.class);
            repository.setUrl(url);
            repository.setName(name);

            repository.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return repository;
    }

    @Override
    public Repository getRepositoryByName(String name){
        Query query = Query.select().where("NAME = ?", name);
        List<Repository> repositoryList = newArrayList(this.ao.find(Repository.class, query));

        if(repositoryList.size() > 0)
            return repositoryList.get(0);

        else
            return null;
    }

    @Override
    public Repository getRepositoryByURL(String url) {
        Query query = Query.select().where("URL = ?", url);
        List<Repository> repositoryList = newArrayList(this.ao.find(Repository.class, query));

        if(repositoryList.size() > 0)
            return repositoryList.get(0);

        else
            return null;
    }

    @Override
    public void updateRepository(String url, String name, long repoId) {
        Repository repository = this.ao.get(Repository.class, (int)repoId);
        repository.setUrl(url);
        repository.setName(name);

        try{

            repository.save();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
