package org.polarion.svnwebclient.web.servlet;

import com.kintosoft.jira.servlet.SWCPluginHttpRequestWrapper;
import org.apache.log4j.Logger;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.AuthenticationException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.util.FileUtil;
import org.polarion.svnwebclient.web.AttributeStorage;
import org.polarion.svnwebclient.web.controller.ChangeConfirmation;
import org.polarion.svnwebclient.web.model.data.ChangeResult;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.ActionUploader;
import org.polarion.svnwebclient.web.support.FormParameters;
import org.polarion.svnwebclient.web.support.GeneralUploader.UploadInfo;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.http.HttpSession;
import java.io.File;

public abstract class BaseFileCommitAction {

	public static String DESTINATION_DIRECTORY = "destinationDirectory";

	protected Logger logger = Logger.getLogger(this.getClass());

	public void execute(IDataProvider dataProvider, State state,
			AbstractRequestHandler requestHandler, boolean isUpload)
			throws SVNWebClientException {

		this.logger.debug("File update/add action");

		// upload file
		this.logger.debug("Uploading file");
		ActionUploader uploader = new ActionUploader();
		UploadInfo uploadInfo = uploader
				.upload(state, requestHandler, isUpload);
		if (!uploadInfo.isUploaded()) {
			this.logger.warn("File wasn't uploaded");
			return;
		}

		String destinationDirectory = uploadInfo.getDestinationDir();
		String name = uploadInfo.getName();
		String comment = uploadInfo.getComment();

		this.logger.debug("File was successfully uploaded. " + "Path: "
				+ destinationDirectory + ", file name: " + name);

		// commit file to repository
		this.logger.debug("Committing file");
		boolean isDeleteUploadedDirectory = true;
		try {
			ChangeResult changeResult = this.commitFile(uploadInfo,
					dataProvider, requestHandler);
			this.logger.debug("File was successfully committed");

			state.getRequest()
					.getSession()
					.setAttribute(ChangeConfirmation.CHANGE_RESULT,
							changeResult);

			// forward to next page
			this.goToNextPage(state);

		} catch (AuthenticationException ae) {
			/*
			 * If there's authentication exception during file commit, store
			 * info about uploaded file in session in order not to re-upload it
			 * again if user enters correct credentials
			 */
			AttributeStorage handler = AttributeStorage.getInstance();

			HttpSession session = state.getRequest().getSession();
			handler.addParameter(session, FormParameters.FILE_NAME, name);
			handler.addParameter(session, FormParameters.COMMENT, comment);
			handler.addParameter(session, DESTINATION_DIRECTORY,
					destinationDirectory);

			isDeleteUploadedDirectory = false;

			throw ae;
		} finally {
			// delete temporary directory
			if (isDeleteUploadedDirectory) {
				FileUtil.deleteDirectory(new File(destinationDirectory));
			}
		}
	}

	protected void goToNextPage(State state) throws SVNWebClientException {
		try {
			SWCPluginHttpRequestWrapper.forward(state.getRequest(),
					state.getResponse(), Links.CHANGE_CONFIRMATION);
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected abstract ChangeResult commitFile(UploadInfo uploadInfo,
			IDataProvider dataProvider, AbstractRequestHandler requestHandler)
			throws SVNWebClientException;
}
