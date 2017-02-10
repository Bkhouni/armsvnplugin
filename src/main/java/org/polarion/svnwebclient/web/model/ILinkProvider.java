package org.polarion.svnwebclient.web.model;

public interface ILinkProvider {
	String getDirectoryContentLink();

	String getFileContentLink();

	boolean isPickerMode();
}
