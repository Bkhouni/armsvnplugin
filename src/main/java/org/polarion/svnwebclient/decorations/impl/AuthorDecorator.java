package org.polarion.svnwebclient.decorations.impl;

import org.polarion.svnwebclient.decorations.IAuthorDecorator;

public class AuthorDecorator implements IAuthorDecorator {

	public String getAuthorName(String authorId) {
		return authorId;
	}

}
