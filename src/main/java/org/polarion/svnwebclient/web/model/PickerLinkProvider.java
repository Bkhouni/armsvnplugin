package org.polarion.svnwebclient.web.model;

import org.polarion.svnwebclient.web.resource.Links;

public class PickerLinkProvider implements ILinkProvider {

	public String getDirectoryContentLink() {
		return Links.PICKER_DIRECTORY_CONTENT;
	}

	public String getFileContentLink() {
		return null;
	}

	public boolean isPickerMode() {
		return true;
	}

}
