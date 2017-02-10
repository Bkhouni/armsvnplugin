package org.polarion.svnwebclient.web.model;

import org.polarion.svnwebclient.web.resource.Links;

public class GeneralLinkProvider implements ILinkProvider {

	public String getDirectoryContentLink() {
		return Links.DIRECTORY_CONTENT;
	}

	public String getFileContentLink() {
		return Links.FILE_CONTENT;
	}

	public boolean isPickerMode() {
		return false;
	}

}
