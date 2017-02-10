package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.ao.Action;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IActionManager;
import net.java.ao.Query;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 07/09/2016.
 */

@Transactional
public class ActionManager implements IActionManager{
    private final ActiveObjects ao;

    public ActionManager(ActiveObjects ao){
        this.ao = ao;
    }

    @Override
    public Action getActionById(Integer id) {
        return null;
    }

    @Override
    public Action addAction(Repository repo, Revision revision, String actionString, Item item) {
        Action action = null;
        try{

            action = this.ao.create(Action.class);
            action.setRepo(repo);
            action.setRevision(revision);
            action.setAction(actionString);
            action.setItem(item);
            action.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return action;
    }


    @Override
    public List<Action> getActionItemByRepoByRevision(int repoId, int revisionId) {
//        Query myQuery = Query.select("ACTION.action, ITEM.path, ITEM.name, ACTION.revision").where("ACTION.itemid=i.id and ACTION.repoId=? and ACTION.revision=? ", repoId, revision).join(Item.class,"A");

        Iterable<String> rs =null;

        Repository repository = this.ao.get(Repository.class, repoId);
        Revision revision = this.ao.get(Revision.class, revisionId);

        List<Action> actions = newArrayList(this.ao.find(Action.class, Query.select()
                .where("action.REPO_ID = ? AND action.REVISION_ID = ?", repository, revision)
                .alias(Action.class, "action").alias(Item.class,"item")
                .join(Item.class, "action.item_ID = item.ID").order("item.PATH, item.NAME")));


        return actions;
    }


}
