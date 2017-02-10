package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;

import  java.util.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IRevisionManager {
    Revision getRevisionById(int id);
    Revision addRevision(long rev,Repository repo, String author, Timestamp timestamp, Date day);
    List<Revision> getRevisionsByRepo(int repoId);
    long getRevisionsByRepoCount(int repoId);
    List<Revision> getRevisionsByRepoByRevision(int repoId, int revision);

}
