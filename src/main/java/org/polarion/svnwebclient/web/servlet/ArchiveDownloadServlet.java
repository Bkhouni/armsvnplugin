package org.polarion.svnwebclient.web.servlet;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.polarion.svnwebclient.SVNWebClientException;
import org.polarion.svnwebclient.data.DataProviderException;
import org.polarion.svnwebclient.data.IDataProvider;
import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.data.model.DataFileElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.util.UrlUtil;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestHandler;
import org.polarion.svnwebclient.web.support.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class ArchiveDownloadServlet extends AbstractServlet {

	private static final long serialVersionUID = 3025332777443975638L;

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider, State state)
			throws SVNWebClientException {
		AbstractRequestHandler requestHandler = this.getRequestHandler(state
				.getRequest());

		// init
		long revision;
		String url;
		if (requestHandler.getCurrentRevision() != -1) {
			revision = requestHandler.getCurrentRevision();
		} else {
			revision = dataProvider.getHeadRevision();
		}
		String strUrl = requestHandler.getUrl();

		if (requestHandler.getPegRevision() != -1) {
			strUrl = dataProvider.getLocation(requestHandler.getUrl(),
					requestHandler.getPegRevision(), revision);
		}
		url = strUrl;
		DataRepositoryElement info = dataProvider.getInfo(url, revision);

		// execute
		ZipArchiveOutputStream zos = null;
		try {
			HttpServletResponse response = state.getResponse();
			OutputStream out = response.getOutputStream();

			zos = new ZipArchiveOutputStream(out);
			zos.setEncoding("Cp437");
			zos.setFallbackToUTF8(true);
			zos.setUseLanguageEncodingFlag(true);
			zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

			String mimeType = "application/octet-stream";
			response.setContentType(mimeType);

			String fileName = info.getName();
			fileName = fileName + "-r" + Long.toString(info.getRevision())
					+ ".zip";
			String filenameAttr = UrlUtil.getFilenameAttribute(fileName,
					state.getRequest());

			response.setHeader("Content-Disposition", "attachment; "
					+ filenameAttr);

			DataDirectoryElement dir = dataProvider.getDirectory(url, revision,
					false);
			this.processFolder(dataProvider, zos, dir, "", url);

			zos.flush();
		} catch (Exception ie) {
			throw new SVNWebClientException(ie);
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	protected void processFolder(IDataProvider dataProvider,
			ZipArchiveOutputStream zip, DataDirectoryElement dir, String path,
			String url) throws SVNWebClientException, IOException {
		if (dir.isRestricted() || dir.getDate() == null) {
			// try to access the directory in case we are logged in.
			try {
				dir = dataProvider.getDirectory(url, dir.getRevision());
			} catch (Exception ex) {
				// We probably have insufficient rights to view this directory.
				// System.out.println("Restricted  dir: /skip " + url + path +
				// " " + ex.getMessage());
				return;
			}
		} else {
			// normal directory
			// if you load all children, the next line is not needed. However,
			// that causes trouble with restricted folders
			try {
				dir = dataProvider.getDirectory(url, dir.getRevision());
			} catch (DataProviderException e) {
				dir = dataProvider.getDirectory(url, -1);
			}
		}

		List children = dir.getChildElements();
		if (children == null || children.size() == 0) {
			// empty folder
			if (path.length() == 0) {
				return;
			} else {
				ZipArchiveEntry ze = new ZipArchiveEntry(path);
				ze.setComment("Empty directory");
				zip.putArchiveEntry(ze);
				return;
			}
		} else {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				// write files first
				DataRepositoryElement el = (DataRepositoryElement) it.next();
				String childUrl = dataProvider.getLocation(
						url + "/" + el.getName(), dir.getRevision(),
						el.getRevision());
				if (el.isDirectory()) {
					// add folder
					String childPath;
					if (path.length() == 0) {
						childPath = el.getName() + "/";
					} else {
						childPath = path + el.getName() + "/";
					}
					this.processFolder(dataProvider, zip,
							((DataDirectoryElement) el), childPath, childUrl);
				} else {
					String childPath;
					if (path.length() == 0) {
						childPath = el.getName();
					} else {
						childPath = path + el.getName();
					}
					// export file
					this.processFile(dataProvider, zip, ((DataFileElement) el),
							childPath, childUrl);
				}
			}
		}
	}

	protected void processFile(IDataProvider dataProvider,
			ZipArchiveOutputStream zip, DataFileElement file, String path,
			String url) throws SVNWebClientException, IOException {
		String containerMimeType = this.getServletContext().getMimeType(
				file.getName().toLowerCase());

		try {
			file = dataProvider.getFile(url, file.getRevision(),
					containerMimeType);
		} catch (DataProviderException de) {
			file = dataProvider.getFile(url, -1, containerMimeType);
		}
		// System.out.println("file entry: " + filePath);
		// add file

		ZipArchiveEntry ze = new ZipArchiveEntry(path);
		byte[] data = file.getContent();
		ze.setSize(data.length);
		zip.putArchiveEntry(ze);
		zip.write(data);
		zip.closeArchiveEntry();
	}
}
