package com.kintosoft.svnwebclient.graph.entities.managers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.user.ApplicationUser;
import com.kintosoft.svnwebclient.db.DatabaseFunctions;
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;
import com.kintosoft.svnwebclient.graph.entities.imanagers.IKeyManager;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by Balkis on 07/09/2016.
 */
@Transactional
public class KeyManager implements IKeyManager{
    private final ActiveObjects ao;
    private DatabaseFunctions databaseFunctions;
    private final static Logger log = LoggerFactory
            .getLogger(KeyManager.class);

    public KeyManager(ActiveObjects ao){
        this.ao = ao;
        databaseFunctions = new DatabaseFunctions();
    }
    @Override
    public Key getKeybyId(int id) {
        return this.ao.get(Key.class, id);
    }

    @Override
    public Key addKey(Repository repository, long revision, String project, long issue) {
        Key key = null;

        Revision[] allRepoRevisions = repository.getRevisions();
        Revision revisionObject = null;

        for(Revision rev : allRepoRevisions){

            if(rev.getRevision() == revision)
                revisionObject = rev;

        }


        try{

            key = this.ao.create(Key.class);
            key.setRev(revision);
            key.setRevision(revisionObject);
            key.setRepository(repository);
            key.setProject(project);
            key.setIssue(issue);
            key.save();

        }catch(Exception e){
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public List<Key> getKeysByIssues(Collection<String> issues, int startIndex, int pageSize,
                                     boolean ascending) {
        Query myQuery;
        List<Key> result = new ArrayList<>();

        int count = 0;
        for (String issue : issues) {
            if (log.isDebugEnabled())
                log.debug("Retrieving revisions for : " + issue);
            String projectKey = IssueKey.from(issue).getProjectKey();
            long issueCounter = IssueKey.from(issue).getIssueNumber();

            log.debug("----- project key is : " + projectKey);
            log.debug("----- issue number is : " + issueCounter);
            if (projectKey == null || issueCounter == -1) {
                log.warn("Invalid issue key:" + issue);
                return null;
            }

            myQuery = Query.select().where("PROJECT= ? AND ISSUE=?", projectKey, issueCounter);

            List<Key> iterationKeys = newArrayList(this.ao.find(Key.class, myQuery));
            result.addAll(iterationKeys);
        }

        //ASC
        if(ascending) {
            Collections.sort(result, new Comparator<Key>() {
                @Override
                public int compare(Key o1, Key o2) {
                    return (int)(o1.getRev() - o2.getRev());
                }
            });
        }
        //DESC
        else{
            Collections.sort(result, new Comparator<Key>() {
                @Override
                public int compare(Key o1, Key o2) {
                    return (int)(o2.getRev() - o1.getRev());
                }
            });
        }

        //LIMIT AND OFFSET
        if(result.size() > pageSize){
            if(startIndex > 0){
                result.removeAll(result.subList(0,startIndex-1));
            }
            else {
                result.removeAll(result.subList(pageSize, result.size() - 1));
            }
        }


        return result;
    }

    @Override
    public List<Key> getKeysByProject(String projectKey, ApplicationUser user, int startIndex, int pageSize) {
        Query myQuery = Query.select().where("PROJECT = ?", projectKey);
        List<Key> result = newArrayList(this.ao.find(Key.class, myQuery.order("REVISION_ID DESC")));

        //PRIVILEGES
        for(Key key:result){

            if(! DatabaseFunctions.hasVersionControlPermisions(user.getUsername(),key.getProject(), key.getIssue())){
                result.remove(key);
            }
        }


        //LIMIT AND OFFSET
        if(result.size() > pageSize){
            if(startIndex > 0){
                result.removeAll(result.subList(0,startIndex - 1));
            }else {
                result.removeAll(result.subList(pageSize, result.size() - 1));
            }
        }

        return result;
    }

    /*
    * ACTUAL QUERY IS
    * sql = "select  ROWNUM() as RN, repoId, revision from keys where ISSUEKEY(project,issue) in ('"
				+ issueKeysIn + "') order by revision desc";
    * sql = "select * from (" + sql + ") where RN between ? and ?";
    * */
    @Override
    public List<Key> getKeysByIssueKey(String issueKeyIn, int startIndex, int pageSize) {
        Query myQuery = Query.select().order("REVISION_ID DESC");
        List<Key> result = newArrayList(this.ao.find(Key.class, myQuery));

        //ISSUEKEY CHECK
        for(Key key:result){
            if (!issueKeyIn.contains(DatabaseFunctions.issueKey(key.getProject(),key.getIssue()))){
                result.remove(key);
            }
        }

        if(startIndex > 0){
            result.removeAll(result.subList(0,startIndex - 1));
        }

        if(result.size() > startIndex + pageSize + 2){
            result.removeAll(result.subList(startIndex + pageSize, result.size() -1));
        }


        return result;
    }
}
