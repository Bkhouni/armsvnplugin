package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by Balkis on 07/09/2016.
 */

@Preload
public interface AppConfig extends Entity {

    String getKey();
    void setKey(String key);

    String getValue();
    void setValue(String value);

}
