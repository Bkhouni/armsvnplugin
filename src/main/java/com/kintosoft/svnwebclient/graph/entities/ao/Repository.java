package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;

import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */
@Preload
public interface Repository extends Entity{

    void setID(int repoId);

    String getUrl();
    void setUrl(String url);

    String getName();
    void setName(String name);

    @OneToMany
    public Key[] getKeys();

    @OneToMany
    public Revision[] getRevisions();

    @OneToMany
    public Item[] getItems();

    @OneToMany
    public Action[] getActions();

    @OneToMany
    public Copy[] getCopies();

    @OneToMany
    public Comment[] getComments();

    @OneToMany
    public RepoConfig[] getRepositoryConfigurations();


}
