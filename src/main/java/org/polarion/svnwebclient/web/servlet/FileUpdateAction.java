package org.polarion.svnwebclient.web.servlet;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataChangedElement;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.data.ChangeResult;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.GeneralUploader.UploadInfo;

import java.util.ArrayList;
import java.util.List;

public class FileUpdateAction extends BaseFileCommitAction {

	protected ChangeResult commitFile(UploadInfo uploadInfo,
			IDataProvider dataProvider, AbstractRequestHandler requestHandler)
			throws SVNWebClientException {

		String destinationDirectory = uploadInfo.getDestinationDir();
		String name = uploadInfo.getName();
		String comment = uploadInfo.getComment();

		this.logger.debug("Updating file. Url: " + requestHandler.getUrl());

		DataChangedElement changedElement = dataProvider.commitFile(
				requestHandler.getUrl(), destinationDirectory + "/" + name,
				comment);

		Navigation navigation = new Navigation(dataProvider.getId(),
				requestHandler.getUrl(), requestHandler.getLocation(),
				FileUpdateActionServlet.CURRENT_REVISION, true);

		List elements = new ArrayList();
		ChangeResult.Element element = new ChangeResult.Element();
		element.setAuthor(changedElement.getAuthor());
		element.setComment(changedElement.getComment());
		element.setDate(changedElement.getDate());
		element.setDirectory(false);
		element.setName(changedElement.getName());
		element.setRevision(changedElement.getRevision());
		elements.add(element);
		String message = "File " + changedElement.getName()
				+ " was succesfully commited";
		ChangeResult changeResult = new ChangeResult(true, message, elements,
				navigation);

		return changeResult;
	}

}
