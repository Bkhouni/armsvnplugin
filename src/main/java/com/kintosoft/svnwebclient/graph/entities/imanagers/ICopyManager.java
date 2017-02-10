package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.Copy;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface ICopyManager {
    Copy getCopyById(Integer id);
    Copy addCopy(Repository repo, Revision revision, Item item, Revision fromrevision, Item fromitem);

}
