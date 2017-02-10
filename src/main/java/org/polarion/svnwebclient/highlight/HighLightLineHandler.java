package org.polarion.svnwebclient.highlight;

import com.uwyn.jhighlight.renderer.Renderer;

import java.io.IOException;

public class HighLightLineHandler {
	protected final static String LINE_SEPARATOR = "\\r\\n|\\r|\\n";
	protected final HighLighter highlighter = HighLighter.getHighLighter();

	protected final Renderer renderer;
	protected String[] lines;
	protected int pos = 0;

	public HighLightLineHandler(String content, String fileExtension) {
		this.renderer = this.highlighter.getRenderer(fileExtension);
		this.lines = content.split(HighLightLineHandler.LINE_SEPARATOR);
	}

	public boolean hasLines() {
		return this.pos < this.lines.length;
	}

	public String getLine() throws IOException {
		String line = this.lines[this.pos++];
		String res = this.highlighter.highlight(line, this.renderer);
		return res;
	}

	public int size() {
		return this.lines.length;
	}

	// public static void main(String[] s) throws Exception {
	// File file = new
	// File("d:/Workspaces/polarion/SVN Web Client/org.polarion.svnwebclient/plugin.properties");
	// FileReader in = new FileReader(file);
	// char[] cbuf = new char[(int) file.length()];
	// in.read(cbuf, 0, cbuf.length);
	//
	// String content = new String(cbuf);
	// //System.out.println(content);
	//
	// String extension = "txt";
	//
	// HighLightLineHandler handler = new HighLightLineHandler(content,
	// extension);
	// int size = handler.size();
	// System.out.println("size: " + size);
	//
	// while (handler.hasLines()) {
	// String line = handler.getLine();
	// System.out.println(line);
	// }
	//
	// }
}
