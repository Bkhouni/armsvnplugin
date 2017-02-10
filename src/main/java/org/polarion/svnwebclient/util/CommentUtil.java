package org.polarion.svnwebclient.util;

public class CommentUtil {
	protected final static String TAG_BR = "<br>";
	protected final static String REGEXP = "\\r?\\n";

	public static String getFirstLine(String comment) {
		String res = "";

		String[] mas = comment.split(CommentUtil.REGEXP);
		if (mas != null && mas.length > 0) {
			String str = mas[0];
			res = HtmlUtil.encode(str);
		}
		return res;
	}

	public static String getTooltip(String comment) {
		String res = "";
		if (comment != null) {
			res = HtmlUtil.encode(comment);
			res = res.replaceAll(CommentUtil.REGEXP, CommentUtil.TAG_BR);
		}
		return res;
	}

	public static boolean isMultiLine(String comment) {
		String str = comment.replaceFirst(CommentUtil.REGEXP, " ");
        return !str.equals(comment);
	}
}
