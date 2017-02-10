package com.kintosoft.svnwebclient.graph.entities.imanagers;

import com.atlassian.jira.user.ApplicationUser;
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.graph.entities.ao.Repository;
import com.kintosoft.svnwebclient.graph.entities.ao.Revision;

import java.util.Collection;
import java.util.List;

/**
 * Created by Balkis on 07/09/2016.
 */
public interface IKeyManager {
    Key getKeybyId(int id);
    Key addKey(Repository repository, long revision, String project, long issue);
    List<Key> getKeysByIssues(Collection<String> issues, int startIndex, int pageSize,
                              boolean ascending);

    List<Key> getKeysByProject(String projectKey,
                               ApplicationUser user, int startIndex, int pageSize);

    List<Key> getKeysByIssueKey(String IssueKeyIn, int startIndex, int pageSize);
}
