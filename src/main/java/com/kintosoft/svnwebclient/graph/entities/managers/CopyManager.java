package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.ao.Copy;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.graph.entities.imanagers.ICopyManager;

/**
 * Created by Balkis on 13/09/2016.
 */
@Transactional
public class CopyManager implements ICopyManager {

    private final ActiveObjects ao;

    public CopyManager(ActiveObjects ao){
        this.ao = ao;
    }

    @Override
    public Copy getCopyById(Integer id) {
        return null;
    }

    @Override
    public Copy addCopy(Repository repo, Revision revision, Item item, Revision fromrevision, Item fromitem) {
        Copy copy = null;
        try{

            copy = this.ao.create(Copy.class);
            copy.setRepo(repo);
            copy.setRevision(revision);
            copy.setItem(item);
            copy.setFromRevision(fromrevision);
            copy.setFromItem(fromitem);
            copy.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return copy;

    }
}
