package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IItemManager;
import net.java.ao.Query;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 13/09/2016.
 */

@Transactional
public class ItemManager implements IItemManager {

    private final ActiveObjects ao;

    public ItemManager(ActiveObjects ao){
        this.ao = ao;
    }


    @Override
    public Item getItemById(int id) {
        return this.ao.get(Item.class, id);
    }

    @Override
    public Item getItemByValues(Repository repo, String path, String name) {
        List<Item> items= newArrayList(this.ao.find(Item.class, Query.select().where("REPO_ID = ? AND PATH = ? AND NAME = ?", repo.getID(), path, name)));
        if(items.size() > 0)
            return items.get(0);
        return null;
    }

    @Override
    public Item addItem(Repository repo, String name, String path) {
        Item item = null;
        try{

            item = this.ao.create(Item.class);
            item.setRepo(repo);
            item.setName(name);
            item.setPath(path);
            item.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return item;
    }
}
