package com.kintosoft.svnwebclient.graph.entities.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;

import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */

@Preload
public interface Revision extends Entity{

    long getRevision();
    void setRevision(long revision);

    Repository getRepo();
    void setRepo(Repository repo);

    String getAuthor();
    void setAuthor(String author);

    Timestamp getRTimestamp();
    void setRTimestamp(Timestamp timestamp);

    Date getDay();
    void setDay(Date day);


    @OneToMany
    public Key[] getKeys();

    @OneToMany
    public Comment[] getComments();

    @OneToMany
    public Action[] getActions();
}
