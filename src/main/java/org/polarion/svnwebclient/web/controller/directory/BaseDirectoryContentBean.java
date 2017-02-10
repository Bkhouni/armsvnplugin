package org.polarion.svnwebclient.web.controller.directory;

import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.web.controller.AbstractBean;
import org.polarion.svnwebclient.web.model.sort.DirectoryContentSortManager;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseDirectoryContentBean extends AbstractBean {
	protected long headRevision;
	protected long revision;
	protected String url;
	protected DataDirectoryElement directoryElement;
	protected DirectoryContentSortManager sortManager;
	protected IDataProvider dataProvider;

	public BaseDirectoryContentBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.dataProvider = dataProvider;
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		}
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.revision);
		}
		this.sortManager = new DirectoryContentSortManager(this.state,
				this.requestHandler);
		this.directoryElement = dataProvider.getDirectory(this.url,
				this.revision);

        return this.executeExtraFunctionality();
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public DirectoryContentSortManager getSortManager() {
		return this.sortManager;
	}

	protected abstract boolean executeExtraFunctionality();
}
