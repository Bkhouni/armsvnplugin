package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.Action;
import com.kintosoft.svnwebclient.graph.entities.ao.Item;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;

import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IActionManager {
    Action getActionById(Integer id);
    Action addAction(Repository repo, Revision revision, String action, Item item);
    List<Action> getActionItemByRepoByRevision(int repoId, int revision);
}
