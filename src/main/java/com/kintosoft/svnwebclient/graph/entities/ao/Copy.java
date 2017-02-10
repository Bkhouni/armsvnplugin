package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by Balkis on 07/09/2016.
 */
@Preload
public interface Copy extends Entity {

    Repository getRepo();
    void setRepo(Repository id);

    Revision getRevision();
    void setRevision(Revision revision);

    Item getFromItem();
    void setFromItem(Item fromItem);

    Item getItem();
    void setItem(Item item);

    Revision getFromRevision();
    void setFromRevision(Revision revision);
}
