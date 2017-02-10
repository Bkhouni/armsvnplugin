package org.polarion.svnwebclient.web.servlet;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.IncorrectParameterException;
import org.polarion.svnwebclient.data.model.DataChangedElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.ChangeResult;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.GeneralUploader.UploadInfo;

import java.util.ArrayList;
import java.util.List;

public class FileAddAction extends BaseFileCommitAction {

	protected ChangeResult commitFile(UploadInfo uploadInfo,
			IDataProvider dataProvider, AbstractRequestHandler requestHandler)
			throws SVNWebClientException {

		ChangeResult changeResult = null;

		Navigation navigation = new Navigation(dataProvider.getId(),
				requestHandler.getUrl(), requestHandler.getLocation(),
				FileAddActionServlet.CURRENT_REVISION, false);

		String name = uploadInfo.getName();

		try {
			String fileUrl = requestHandler.getUrl();
			if (!fileUrl.endsWith("/")) {
				fileUrl += "/";
			}
			fileUrl += name;

			long revision = dataProvider.getHeadRevision();
			/*
			 * Check if file already exists in repository, if it exists, then do
			 * nothing, otherwise add file
			 */
			DataRepositoryElement dataRepositoryElement = dataProvider.getInfo(
					fileUrl, revision);

			this.logger.debug("Unable to add file, because it already exist. "
					+ "Url: " + fileUrl + ", revision: " + revision);

			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(dataRepositoryElement.getAuthor());
			element.setComment(dataRepositoryElement.getComment());
			element.setDate(dataRepositoryElement.getDate());
			element.setDirectory(false);
			element.setName(dataRepositoryElement.getName());
			element.setRevision(dataRepositoryElement.getRevision());
			elements.add(element);
			String message = "File " + name
					+ " already exists in specified location";
			changeResult = new ChangeResult(false, message, elements,
					navigation);
		} catch (IncorrectParameterException ie) {
			this.logger.debug("Adding file. " + "Url: "
					+ requestHandler.getUrl());

			DataChangedElement changedElement = dataProvider.addFile(
					requestHandler.getUrl(), uploadInfo.getDestinationDir()
							+ "/" + name, uploadInfo.getComment());

			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(changedElement.getAuthor());
			element.setComment(changedElement.getComment());
			element.setDate(changedElement.getDate());
			element.setDirectory(false);
			element.setName(changedElement.getName());
			element.setRevision(changedElement.getRevision());
			elements.add(element);
			String message = "File " + name + " was succesfully added";
			changeResult = new ChangeResult(true, message, elements, navigation);
		}

		return changeResult;
	}

}
