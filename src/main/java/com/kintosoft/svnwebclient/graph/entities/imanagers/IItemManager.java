package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IItemManager {
    Item getItemById(int id);
    Item getItemByValues(Repository repo, String path, String name);
    Item addItem(Repository repo, String name, String path);
}
