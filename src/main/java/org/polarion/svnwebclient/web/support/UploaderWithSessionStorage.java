package org.polarion.svnwebclient.web.support;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.web.AttributeStorage;
import org.polarion.svnwebclient.web.servlet.BaseFileCommitAction;
import org.polarion.svnwebclient.web.support.GeneralUploader.UploadInfo;

import javax.servlet.http.HttpSession;
import java.io.File;

public class UploaderWithSessionStorage {

	/**
	 * 
	 * First try to retrieve attributes from session, it could happen because of
	 * authentication error on committing uploaded file; in this case we don't
	 * need to perform file uploaded as it is already uploaded, but only not
	 * committed
	 * 
	 */
	public UploadInfo upload(State state) throws SVNWebClientException {
		UploadInfo res = null;

		String name = null;
		String comment = null;
		String destinationDir = null;

		AttributeStorage storage = AttributeStorage.getInstance();
		HttpSession session = state.getRequest().getSession();
		name = (String) storage.getParameter(session, FormParameters.FILE_NAME);
		comment = (String) storage
				.getParameter(session, FormParameters.COMMENT);
		destinationDir = (String) storage.getParameter(session,
				BaseFileCommitAction.DESTINATION_DIRECTORY);

		boolean wasPreviouslyUploaded = name != null && destinationDir != null;

		if (wasPreviouslyUploaded) {
			// delete previous session paramaters
			AttributeStorage.getInstance().cleanSession(
					state.getRequest().getSession());

			// check that destination directory contains uploaded file
			File destFile = new File(destinationDir, name);
			if (!destFile.exists()) {
				throw new SVNWebClientException(
						"Unable to upload file, because it doesn't exist. "
								+ "Path: " + destFile.getAbsolutePath());
			}

			res = new UploadInfo();
			res.setName(name);
			res.setComment(comment);
			res.setDestinationDir(destinationDir);
			res.setUploaded(true);
		} else {
			GeneralUploader uploader = new GeneralUploader();
			res = uploader.upload(state);
		}

		return res;
	}
}
