/* Copyright (C) 2007 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration
 * (NASA).  All Rights Reserved.
 *
 * This software is distributed under the NASA Open Source Agreement
 * (NOSA), version 1.3.  The NOSA has been approved by the Open Source
 * Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
 * directory tree for the complete NOSA document.
 *
 * THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
 * KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
 * LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
 * SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
 * THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
 * DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
 */
package gov.nasa.jpf.shell.panels;

import javax.swing.text.*;
import gov.nasa.jpf.shell.util.EditorPanel;
import gov.nasa.jpf.shell.ShellManager;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Panel that serves as an editor for the file pointed to in the *.jpf file
 * denoted in the "jpf.app" property in
 * {@link gov.nasa.jpf.shell.ShellManager#getConfig() }. Implements basic text
 * editing features and syntax highlighting.
 */
public class PropertiesPanel extends EditorPanel {

	private static final String DEFAULT_TITLE = "Properties";
	private static final String DEFAULT_TIP = "Edit the Application Properties";
	private static final String MIME_TYPE = "text/jpfproperties";
	protected JLabel newLabel = new JLabel("File: ", JLabel.RIGHT);

	public PropertiesPanel() {
		super(DEFAULT_TITLE, null, DEFAULT_TIP);

		String fName = ShellManager.getManager().getConfig().getProperty("jpf.app");

		initContents("JPF application properties", fName);

		this.errorField.setText("");
	}

	protected PropertiesPanel(String title, Icon icon, String tip) {
		super(title, icon, tip);
	}

	protected EditorKit getEditorKit() {
		return new StyledEditorKit() {

			@Override
			public Document createDefaultDocument() {
				return new PropertyDocument(errorField);
			}

			@Override
			public String getContentType() {
				return MIME_TYPE;
			}
		};

	}

	@Override
	protected void fileSaved(File f) {
		ShellManager.getManager().reloadAppProperties(loadedFile.getPath());
	}

	@Override
	protected void fileLoaded(File f) {
		if (initialized) {
			// no need to rebuild if this is from the ctor
			ShellManager.getManager().reloadAppProperties(loadedFile.getPath());
		}
	}

	@Override
	protected FileNameExtensionFilter getFileFilter() {
		return new FileNameExtensionFilter("JPF application properties", "jpf");
	}
}