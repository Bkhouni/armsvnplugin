package org.polarion.svnwebclient.web.model;

public class LinkProviderFactory {
	public final static String PICKER_CONTENT_MODE_VALUE = "pickerContentMode";

	public static ILinkProvider getLinkProvider(String linkType) {
		ILinkProvider provider = null;

		if (LinkProviderFactory.PICKER_CONTENT_MODE_VALUE.equals(linkType)) {
			provider = new PickerLinkProvider();
		} else {
			provider = new GeneralLinkProvider();
		}

		return provider;
	}
}
