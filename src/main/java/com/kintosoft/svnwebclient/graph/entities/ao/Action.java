package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by Balkis on 07/09/2016.
 */
@Preload
public interface Action extends Entity {

    Revision getRevision();
    void setRevision(Revision revision);

    String getAction();
    void setAction(String action);

    Repository getRepo();
    void setRepo(Repository repo);


    Item getItem();
    void setItem(Item item);

}