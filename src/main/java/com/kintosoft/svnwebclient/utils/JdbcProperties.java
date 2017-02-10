package com.kintosoft.svnwebclient.utils;

/**
 * Created by Balkis on 12/09/2016.
 */

import static com.google.common.base.Preconditions.checkNotNull;

public final class JdbcProperties
{
    public final String url;
    public final String username;
    public final String passord;

    public JdbcProperties(String url, String username, String passord)
    {
        this.url = checkNotNull(url);
        this.username = checkNotNull(username);
        this.passord = checkNotNull(passord);
    }
}
