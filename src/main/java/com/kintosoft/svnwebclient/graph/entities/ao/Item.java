package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;

import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */

@Preload
public interface Item extends Entity{

    Repository getRepo();
    void setRepo(Repository id);

    String getName();
    void setName(String name);

    String getPath();
    void setPath(String path);

    @OneToMany
    public Copy[] getCopies();

    @OneToMany
    public Action[] getActions();
}
