package org.polarion.svnwebclient.util.contentencoding;

import org.polarion.svnwebclient.configuration.ConfigurationException;
import org.polarion.svnwebclient.configuration.ConfigurationProvider;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class ContentEncodingHelper {

	// BOMs and corresponding encoding names are copied from former class
	// org.polarion.svnwebclient.util.contentencoding.UnicodeInputStream
	private static final byte[] BOM_UTF32BE = new byte[] { (byte) 0x00,
			(byte) 0x00, (byte) 0xFE, (byte) 0xFF };
	private static final byte[] BOM_UTF32LE = new byte[] { (byte) 0xFF,
			(byte) 0xFE, (byte) 0x00, (byte) 0x00 };
	private static final byte[] BOM_UTF8 = new byte[] { (byte) 0xEF,
			(byte) 0xBB, (byte) 0xBF };
	private static final byte[] BOM_UTF16BE = new byte[] { (byte) 0xFE,
			(byte) 0xFF };
	private static final byte[] BOM_UTF16LE = new byte[] { (byte) 0xFF,
			(byte) 0xFE };

	public static String encodeBytes(byte[] bytes, String encoding)
			throws IOException {
		String unicodeEncoding = null;
		int offset = 0;
		if (hasBOM(bytes, BOM_UTF32BE)) {
			unicodeEncoding = "UTF-32BE";
			offset = BOM_UTF32BE.length;
		} else if (hasBOM(bytes, BOM_UTF32LE)) {
			unicodeEncoding = "UTF-32LE";
			offset = BOM_UTF32LE.length;
		} else if (hasBOM(bytes, BOM_UTF8)) {
			unicodeEncoding = "UTF-8";
			offset = BOM_UTF8.length;
		} else if (hasBOM(bytes, BOM_UTF16BE)) {
			unicodeEncoding = "UTF-16BE";
			offset = BOM_UTF16BE.length;
		} else if (hasBOM(bytes, BOM_UTF16LE)) {
			unicodeEncoding = "UTF-16LE";
			offset = BOM_UTF16LE.length;
		}

		if ((encoding == null) && Charset.isSupported(unicodeEncoding)) {
			encoding = unicodeEncoding;
		}

		if (encoding != null) {
			return new String(bytes, offset, bytes.length - offset, encoding);
		} else {
			return new String(bytes, offset, bytes.length - offset);
		}
	}

	private static boolean hasBOM(byte[] bytes, byte[] bom) {
		if (bytes.length < bom.length) {
			return false;
		}
		for (int i = 0; i < bom.length; i++) {
			if (bom[i] != bytes[i]) {
				return false;
			}
		}
		return true;
	}

	public static String getEncoding(String id, State state)
			throws ConfigurationException {
		String encoding = state.getRequest().getParameter(
				RequestParameters.CHARACTER_ENCODING);
		if (encoding == null) {
			encoding = (String) state.getSession().getAttribute(
					RequestParameters.DEFAULT_CHARACTER_ENCODING);
			if (encoding == null) {
				encoding = ConfigurationProvider.getInstance(id)
						.getDefaultEncoding();
			}
		} else {
			state.getSession().setAttribute(
					RequestParameters.DEFAULT_CHARACTER_ENCODING, encoding);
		}
		return encoding;
	}

	public static Collection getCharacterEncodings(String id)
			throws ConfigurationException {
		Set encodings = ConfigurationProvider.getInstance(id)
				.getCharacterEncodings();
		encodings.add(ConfigurationProvider.getInstance(id)
				.getDefaultEncoding());

		List res = new ArrayList();
		res.addAll(encodings);
		Collections.sort(res, new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1 = (String) o1;
				String s2 = (String) o2;
				return s1.compareTo(s2);
			}
		});
		return res;
	}

	public static boolean isSelectedCharacterEncoding(String id, State state,
			String encoding) throws ConfigurationException {
		boolean isSelectedCharacterEncoding = false;

		String defaultEncoding = null;
		defaultEncoding = (String) state.getSession().getAttribute(
				RequestParameters.DEFAULT_CHARACTER_ENCODING);
		if (defaultEncoding == null) {
			defaultEncoding = ConfigurationProvider.getInstance(id)
					.getDefaultEncoding();
		}
		if (encoding.equals(defaultEncoding)) {
			isSelectedCharacterEncoding = true;
		}
		return isSelectedCharacterEncoding;
	}
}
