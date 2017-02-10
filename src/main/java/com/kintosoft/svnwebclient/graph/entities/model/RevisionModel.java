package com.kintosoft.svnwebclient.graph.entities.model;

import  java.util.Date;
import java.sql.Timestamp;

/**
 * Created by Balkis on 07/09/2016.
 */
public class RevisionModel {

    private int id;
    private int repoId;
    private String author;
    private Timestamp timestamp;
    private Date day;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRepoId() {
        return repoId;
    }

    public void setRepoId(int repoId) {
        this.repoId = repoId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
