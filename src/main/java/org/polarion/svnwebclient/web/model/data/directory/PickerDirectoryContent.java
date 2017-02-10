package org.polarion.svnwebclient.web.model.data.directory;

import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.decorations.IRevisionDecorator;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PickerDirectoryContent extends BaseDirectoryContent {

	public PickerDirectoryContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler,
			IRevisionDecorator revisionDecorator) {
		super(directoryElement, requestHandler, revisionDecorator);
	}

	public class Element extends BaseDirectoryContent.BaseElement {

		public Element(DataRepositoryElement repositoryElement) {
			super(repositoryElement);
		}

		public boolean isDirectory() {
			return repositoryElement.isDirectory();
		}

		public String getContentUrl() {
			UrlGenerator urlGenerator = null;
			if (this.repositoryElement.isDirectory()) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.PICKER_DIRECTORY_CONTENT,
						requestHandler.getLocation());
			}

			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL,
					UrlUtil.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV,
						Long.toString(requestHandler.getCurrentRevision()));
			}
			if (requestHandler.isSingleRevisionMode()) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			if (requestHandler.isMultiUrlSelection()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			return urlGenerator.getUrl();
		}

		public String getPickerFullUrl() throws ConfigurationException {
			StringBuffer pickerUrl = new StringBuffer("");
			if (!pickerUrl.toString().endsWith("/")) {
				pickerUrl.append("/");
			}
			String shortUrl = url;
			if (!"".equals(shortUrl)) {
				pickerUrl.append(shortUrl).append("/");
			}
			pickerUrl.append(this.repositoryElement.getName());
			return pickerUrl.toString();
		}

		public String getPickerSelectUrlSrcipt() throws ConfigurationException {
			String url = this.getPickerFullUrl();
			url = url.replaceAll("'", "&comma");

			String res = "\"" + url + "\"";
			return res;
		}

	}

	public List getChilds() {
		List ret = new ArrayList();
		List childElements = this.directoryElement.getChildElements();
		Collections.sort(childElements, this.comparator);
		for (Iterator i = childElements.iterator(); i.hasNext();) {
			ret.add(new Element((DataRepositoryElement) i.next()));
		}
		return ret;
	}
}
