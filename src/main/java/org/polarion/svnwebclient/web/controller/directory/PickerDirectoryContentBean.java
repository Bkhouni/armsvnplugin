package org.polarion.svnwebclient.web.controller.directory;

import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.util.UrlGenerator;
import org.polarion.svnwebclient.util.UrlGeneratorFactory;
import org.polarion.svnwebclient.web.model.ILinkProvider;
import org.polarion.svnwebclient.web.model.Navigation;
import org.polarion.svnwebclient.web.model.PickerLinkProvider;
import org.polarion.svnwebclient.web.model.data.directory.PickerDirectoryContent;
import org.polarion.svnwebclient.web.resource.Links;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class PickerDirectoryContentBean extends BaseDirectoryContentBean {

	protected static final String PICKER_ATTRIBUTE = "pickerInfo";
	protected static final String ACTIONS_PARAM = "actions";
	protected static final String ACTIONS_ADD_VALUE = "add";
	protected static final String ACTIONS_REMOVE_VALUE = "remove";

	protected static final String FLAGS_PARAM = "flags";
	protected static final String PICKER_URL_PARAM = "pickerUrl";
	protected static final String PICKER_REVISION_PARAM = "pickerRevision";
	protected static final String ID_PARAM = "id";
	protected static final String CLEAR_PARAM = "clear";
	protected static final String PICKER_REMOVE_ID = "removeResourceId";

	public PickerDirectoryContentBean() {
	}

	protected boolean executeExtraFunctionality() {
		if (this.state.getRequest().getParameter(
				PickerDirectoryContentBean.CLEAR_PARAM) != null) {
			this.state.getSession().removeAttribute(
					PickerDirectoryContentBean.PICKER_ATTRIBUTE);
		}

		if (PickerDirectoryContentBean.ACTIONS_ADD_VALUE.equals(this.state
				.getRequest().getParameter(
						PickerDirectoryContentBean.ACTIONS_PARAM))) {
			if (!this.performAddAction()) {
				return false;
			}
		} else if (PickerDirectoryContentBean.ACTIONS_REMOVE_VALUE
				.equals(this.state.getRequest().getParameter(
						PickerDirectoryContentBean.ACTIONS_PARAM))) {
			if (!this.performRemoveAction()) {
				return false;
			}
		}

		return true;
	}

	protected boolean performAddAction() {
		HttpSession session = this.state.getSession();

		String[] flags = this.state.getRequest().getParameterValues(
				PickerDirectoryContentBean.FLAGS_PARAM);
		String[] pickerUrls = this.state.getRequest().getParameterValues(
				PickerDirectoryContentBean.PICKER_URL_PARAM);
		String[] pickerRevisions = this.state.getRequest().getParameterValues(
				PickerDirectoryContentBean.PICKER_REVISION_PARAM);

		Map pickerMap = (Map) this.state.getSession().getAttribute(
				PickerDirectoryContentBean.PICKER_ATTRIBUTE);
		int lastId = 0;

		if (pickerMap != null) {
			lastId = this.getMapLastId(pickerMap);
		} else {
			pickerMap = new TreeMap(new Comparator() {
				public int compare(Object ob1, Object ob2) {
					int num1 = Integer.parseInt((String) ob1);
					int num2 = Integer.parseInt((String) ob2);
					return num1 - num2;
				}
			});
			lastId = 0;
			session.setAttribute(PickerDirectoryContentBean.PICKER_ATTRIBUTE,
					pickerMap);
		}

		for (int i = 0; i < flags.length; i++) {
			if ("1".equals(flags[i])) {
				String value = pickerUrls[i] + ";" + pickerRevisions[i];
				if (!pickerMap.containsValue(value)) {
					pickerMap.put(new Integer(++lastId).toString(), value);
				}
			}
		}

		return true;
	}

	protected int getMapLastId(Map pickerMap) {
		Iterator it = pickerMap.keySet().iterator();
		int max = Integer.MIN_VALUE;
		while (it.hasNext()) {
			int num = Integer.parseInt((String) it.next());
			if (num > max) {
				max = num;
			}
		}
		return max;
	}

	protected boolean performRemoveAction() {
		Map pickerMap = (Map) this.state.getSession().getAttribute(
				PickerDirectoryContentBean.PICKER_ATTRIBUTE);
		String removeId = this.state.getRequest().getParameter(
				PickerDirectoryContentBean.PICKER_REMOVE_ID);
		pickerMap.remove(removeId);
		return true;
	}

	public String getCurrentUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.PICKER_DIRECTORY_CONTENT,
				this.requestHandler.getLocation());
		HttpServletRequest request = this.state.getRequest();
		String query = request.getQueryString();
		if (query != null && !"".equals(query)) {
			String[] params = query.split("&");
			for (int i = 0; i < params.length; i++) {
				String para = params[i];
				String paramName = null;
				String paramValue = null;

				int equalSign = para.indexOf("=");
				if (equalSign != -1) {
					paramName = para.substring(0, equalSign);
					paramValue = para.substring(equalSign + 1);
				} else {
					paramName = para;
				}
				if (!PickerDirectoryContentBean.CLEAR_PARAM.equals(paramName)) {
					if (paramValue != null) {
						urlGenerator.addParameter(paramName, paramValue);
					} else {
						urlGenerator.addParameter(paramName);
					}
				}
			}
		}
		return urlGenerator.getUrl();
	}

	public PickerDirectoryContent getPickerDirectoryContent() {
		PickerDirectoryContent ret = new PickerDirectoryContent(
				this.directoryElement, this.requestHandler,
				ConfigurationProvider.getRevisionDecorator());
		ret.setState(this.state);
		ret.setHeadRevision(this.headRevision);
		ret.setUrl(this.url);
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	public Navigation getNavigation() throws ConfigurationException {
		boolean isSingle = this.requestHandler.isSingleRevisionMode();
		boolean isMultiUrl = this.requestHandler.isMultiUrlSelection();

		ILinkProvider linkProvider = new PickerLinkProvider();
		return new Navigation(dataProvider.getId(), this.url,
				this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), false, linkProvider,
				isSingle, isMultiUrl);
	}

	protected boolean isPickerMode() {
		return true;
	}

	public PickerDirectoryContent getDirectoryContent() {
		PickerDirectoryContent ret = new PickerDirectoryContent(
				this.directoryElement, this.requestHandler,
				ConfigurationProvider.getRevisionDecorator());
		ret.setState(this.state);
		ret.setHeadRevision(this.headRevision);
		ret.setUrl(this.url);
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	public boolean isMultiUrlSelection() {
		return this.requestHandler.isMultiUrlSelection();
	}
}
