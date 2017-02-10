package org.polarion.svnwebclient.web.support;

import org.apache.log4j.Logger;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.GeneralUploader.UploadInfo;

import java.io.IOException;

public class ActionUploader {

	/**
	 * If file isn't uploaded, then go to retry page
	 */
	public UploadInfo upload(State state,
			AbstractRequestHandler requestHandler, boolean isUpload)
			throws SVNWebClientException {

		UploaderWithSessionStorage uploader = new UploaderWithSessionStorage();
		UploadInfo uploadInfo = uploader.upload(state);

		if (!uploadInfo.isUploaded()) {
			Logger.getLogger(this.getClass())
					.warn("Did not update "
							+ uploadInfo.getName()
							+ " as the file was empty or didn't exist. Retrying again.");

			this.retryAgain(uploadInfo.getName(), isUpload, requestHandler,
					state);
		}

		return uploadInfo;
	}

	protected void retryAgain(String fileName, boolean isUpload,
			AbstractRequestHandler requestHandler, State state)
			throws SVNWebClientException {

		String errorMessage = "Did not update "
				+ fileName
				+ " as the file was empty or didn't exist. Please, retry again.";

		String retryPage = isUpload ? Links.FILE_UPDATE : Links.FILE_ADD;

		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				retryPage, requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL,
				UrlUtil.encode(requestHandler.getUrl()));
		urlGenerator.addParameter(RequestParameters.RETRY_AGAIN,
				RequestParameters.VALUE_TRUE);
		urlGenerator.addParameter(RequestParameters.ERROR_MESSAGE,
				UrlUtil.encode(errorMessage));
		try {
			state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (IOException e) {
			throw new SVNWebClientException(e);
		}
	}
}
