package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.kintosoft.svnwebclient.graph.entities.ao.AppConfig;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IApplicationConfigurationManager;
import net.java.ao.Query;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 12/09/2016.
 */
public class ApplicationConfigurationManager implements IApplicationConfigurationManager{
    private final ActiveObjects ao;

    public ApplicationConfigurationManager(ActiveObjects ao){
        this.ao = ao;
    }

    @Override
    public AppConfig getApplicationConfigurationByKey(String key) {

        Query query = Query.select().where("KEY = ?", key);
        List<AppConfig> confs = newArrayList(this.ao.find(AppConfig.class, query));

        if(confs.size() > 0)
            return confs.get(0);
        else
            return null;
    }


    @Override
    public AppConfig addApplicationConfiguration(String key, String value) {
        AppConfig appConfig = null;

        try{

            appConfig = this.ao.create(AppConfig.class);
            appConfig.setKey(key);
            appConfig.setValue(value);
            appConfig.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return appConfig;
    }


}
