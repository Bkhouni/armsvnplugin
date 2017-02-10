package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by Balkis on 07/09/2016.
 */

@Preload
public interface Key extends Entity {

    long getRev();
    void setRev(long revision);

    Revision getRevision();
    void setRevision(Revision revision);

    Repository getRepository();
    void setRepository(Repository repository);

    String getProject();
    void setProject(String project);

    long getIssue();
    void setIssue(long issue);

}
