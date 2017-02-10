package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by Balkis on 07/09/2016.
 */

/*@Indexes({
        @Index(name = "repos", methodNames = {"getRepo", "getRevision"}),
})*/

@Preload
public interface Comment extends Entity {

    Repository getRepo();
    void setRepo(Repository id);

    Revision getRevision();
    void setRevision(Revision revision);


    String getComment();
    void setComment(String comment);

}
