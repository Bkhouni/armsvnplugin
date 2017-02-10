package com.kintosoft.svnwebclient.utils;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.managers.*;

/**
 * Created by Balkis on 15/09/2016.
 */

@Transactional
public interface AOManager {

    ActiveObjects getAO();

    ActionManager getActionManager();
    RevisionManager getRevisionManager();
    RepositoryManager getRepositoryManager();
    ApplicationConfigurationManager getApplicationConfigurationManager();
    CommentManager getCommentManager();
    ItemManager getItemManager();
    KeyManager getKeyManager();
    RepositoryConfigurationManager getRepositoryConfigurationManager();
    CopyManager getCopyManager();
}
