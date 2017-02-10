/*
 * Copyright (c) 2004, 2005 Polarion Software, All rights reserved.
 * Email: community@polarion.org
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"). You may not use
 * this file except in compliance with the License. Copy of the License is
 * located in the file LICENSE.txt in the project distribution. You may also
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.polarion.svnwebclient.data.javasvn;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

import java.io.InputStream;

/**
 * 
 * @author <A HREF="mailto:svnbrowser@polarion.org">Polarion Software </A>
 */
public class SVNUtils {

	public static SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath,
			String filePath, InputStream is, long size) throws SVNException {
		try {
			SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();

			editor.openRoot(-1);
			editor.openDir(dirPath, -1);
			editor.openFile(filePath, -1);
			editor.applyTextDelta(filePath, null);

			String chksm = deltaGenerator.sendDelta(filePath, is, editor, true);

			editor.textDeltaEnd(filePath);
			editor.closeFile(filePath, chksm);

			/*
			 * Closes the directory.
			 */
			editor.closeDir();
			/*
			 * Closes the root directory.
			 */
			editor.closeDir();
			return editor.closeEdit();
		} catch (SVNException e) {
			if (editor != null) {
				try {
					editor.abortEdit();
				} catch (Exception ex) {
				}
			}
			throw e;
		}
	}
}
