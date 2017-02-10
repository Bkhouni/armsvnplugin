package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IRevisionManager;
import net.java.ao.Query;

import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 07/09/2016.
 */

@Transactional
public class RevisionManager implements IRevisionManager {
    private final ActiveObjects ao;

    public RevisionManager(ActiveObjects ao){
        this.ao = ao;
    }
    @Override
    public Revision getRevisionById(int id) {
        return this.ao.get(Revision.class, id);
    }

    @Override
    public Revision addRevision(long rev,Repository repo, String author, Timestamp timestamp, Date day) {
        Revision revision = null;
        try{
            revision = this.ao.create(Revision.class);
            revision.setRevision(rev);
            revision.setRepo(repo);
            revision.setAuthor(author);
            revision.setRTimestamp(timestamp);
            revision.setDay(day);
            revision.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return revision;
    }

    @Override
    public List<Revision> getRevisionsByRepo(int repoId) {
        Repository repository = this.ao.get(Repository.class, repoId);
        Query myQuery = Query.select().where("REPO_ID = ?", repository);
        myQuery.setOrderClause("ID DESC");

        List<Revision> result = newArrayList(this.ao.find(Revision.class, myQuery));
        return result;
    }

    @Override
    public long getRevisionsByRepoCount(int repoId) {
        Repository repository = this.ao.get(Repository.class, repoId);
        Query myQuery = Query.select().where("REPO_ID = ?", repository);
        List<Revision> result = newArrayList(this.ao.find(Revision.class, myQuery));
        return result.size();

    }

    @Override
    public List<Revision> getRevisionsByRepoByRevision(int repoId, int  rev) {
        Repository repository = this.ao.get(Repository.class, repoId);
        Query myQuery = Query.select().where("REPO_ID = ? AND REVISION = ? ", repository, rev);
        List<Revision> result = newArrayList(this.ao.find(Revision.class, myQuery));

        return result;
    }


}
