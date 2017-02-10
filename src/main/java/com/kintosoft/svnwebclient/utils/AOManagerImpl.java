package com.kintosoft.svnwebclient.utils;

import com.atlassian.activeobjects.external.ActiveObjects;

import com.kintosoft.svnwebclient.graph.entities.managers.*;
import org.springframework.stereotype.Component;

/**
 * Created by Balkis on 15/09/2016.
 */

public class AOManagerImpl  implements AOManager{


    private final ActiveObjects ao;

    public AOManagerImpl(ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public ActiveObjects getAO(){
        return ao;
    }

    @Override
    public ActionManager getActionManager() {
        return new ActionManager(ao);
    }

    @Override
    public RevisionManager getRevisionManager() {
        return new RevisionManager(ao);
    }

    @Override
    public RepositoryManager getRepositoryManager() {
        return new RepositoryManager(ao);
    }

    @Override
    public ApplicationConfigurationManager getApplicationConfigurationManager() {
        return new ApplicationConfigurationManager(ao);
    }

    @Override
    public CommentManager getCommentManager() {
        return new CommentManager(ao);
    }

    @Override
    public ItemManager getItemManager() {
        return new ItemManager(ao);
    }

    @Override
    public KeyManager getKeyManager() {
        return new KeyManager(ao);
    }

    @Override
    public RepositoryConfigurationManager getRepositoryConfigurationManager() {
        return new RepositoryConfigurationManager(ao);
    }

    @Override
    public CopyManager getCopyManager() {
        return new CopyManager(ao);
    }
}
