package org.polarion.svnwebclient.web.support;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.util.Uploader;
import org.w3c.util.UUID;

import java.util.Map;

public class GeneralUploader {

	public static class UploadInfo {
		protected String name;
		protected String comment;
		protected String destinationDir;
		protected boolean isUploaded;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getDestinationDir() {
			return destinationDir;
		}

		public void setDestinationDir(String destinationDir) {
			this.destinationDir = destinationDir;
		}

		public boolean isUploaded() {
			return isUploaded;
		}

		public void setUploaded(boolean isUploaded) {
			this.isUploaded = isUploaded;
		}
	}

	/**
	 * Uploads file to server and returns result info
	 */
	public UploadInfo upload(State state) throws SVNWebClientException {
		String temporaryDirectory = ConfigurationProvider.getTempDirectory();
		String destinationDir = temporaryDirectory + "/" + UUID.getUUID();

		// do upload
		Uploader uploader = new Uploader();
		uploader.doPost(state.getRequest(), state.getResponse(),
				destinationDir, temporaryDirectory);

		Map parameters = uploader.getParameters();
		String name = (String) parameters.get(FormParameters.FILE_NAME);
		String comment = (String) parameters.get(FormParameters.COMMENT);

		boolean isUploaded = uploader.isUploaded();

		UploadInfo res = new UploadInfo();
		res.setName(name);
		res.setComment(comment);
		res.setDestinationDir(destinationDir);
		res.setUploaded(isUploaded);

		return res;
	}
}
