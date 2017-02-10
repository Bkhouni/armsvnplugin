package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.AppConfig;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IApplicationConfigurationManager {
    AppConfig getApplicationConfigurationByKey(String key);
    AppConfig addApplicationConfiguration(String key, String value);


}
