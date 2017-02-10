package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.Comment;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;

import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface ICommentManager {
    Comment getCommentById(Integer id);
    Comment addComment(Repository repo, Revision revision, String comment);
    List<Comment> getCommentByRepoByRevision(long repoId, long revision);
}
