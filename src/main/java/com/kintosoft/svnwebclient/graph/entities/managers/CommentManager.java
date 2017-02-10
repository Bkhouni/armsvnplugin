package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.ao.Comment;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.graph.entities.imanagers.ICommentManager;
import net.java.ao.Query;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 07/09/2016.
 */

@Transactional
public class CommentManager implements ICommentManager{

    private final ActiveObjects ao;

    public CommentManager(ActiveObjects ao){
        this.ao = ao;
    }

    @Override
    public Comment getCommentById(Integer id) {
        return this.ao.get(Comment.class, id);
    }

    @Override
    public Comment addComment(Repository repo, Revision revision, String commentString) {

        Comment comment = null;
        try{

            comment = this.ao.create(Comment.class);
            comment.setRepo(repo);
            comment.setRevision(revision);
            comment.setComment(commentString);
            comment.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return comment;

    }

    @Override
    public List<Comment> getCommentByRepoByRevision(long repoId, long revision) {
        Query myQuery = Query.select().where("REPO_ID = ? AND REVISION_ID = ? ", repoId, revision);
        List<Comment> result = newArrayList(this.ao.find(Comment.class, myQuery));
        return result;
    }
}
