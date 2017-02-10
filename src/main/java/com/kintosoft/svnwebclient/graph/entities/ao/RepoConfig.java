package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;

import static net.java.ao.schema.StringLength.UNLIMITED;

/**
 * Created by Balkis on 07/09/2016.
 */

@Preload
public interface RepoConfig extends Entity {


    Repository getRepo();
    void setRepo(Repository id);

    String getKey();
    void setKey(String key);

    @StringLength(UNLIMITED)
    String getValue();
    void setValue(String value);

}
