package gov.nasa.jpf.shell.panels;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.shell.ShellManager;
import static jpf.shell.util.editor.Utilities.*;

/**
 * A PropertyDocument is the document type used to represent a *.jpf file. it
 * includes the following styles:
 * <dl>
 * <dt>key</dt>
 * <dd>The style to be applied to a property name</dd>
 * <dt>value</dt>
 * <dd>The style to be applied to a property PropertyDocument pd = new PropertyDocument();value</dd>
 * <dt>comment</dt>
 * <dd>The style applied to a comment in the file.</dd>
 * <dt>malformed</dt>
 * <dd>The style applied to a malformed line.</dd>
 * <dt>assignment</dt>
 * <dd>The style applied to everything else (the '=' sign)</dd>
 * </dl>
 */
class PropertyDocument extends DefaultStyledDocument {

	private Style key;
	private Style value;
	private Style comment;
	private Style malformed;
	private Style assignment;
	private Style invalidPath;
	private Style redefinedAttribute;
	
	private JLabel errorField;

	private LinkedHashMap<Properties, Object> loadedProperties;

	public PropertyDocument(JLabel errorField) {
		super();
		loadConfiguration();
		this.errorField = errorField;

		key = addStyle("key", null);
		key.addAttribute(StyleConstants.Foreground, Color.BLUE);

		value = addStyle("value", null);
		value.addAttribute(StyleConstants.Foreground, Color.darkGray);

		comment = addStyle("comment", null);
		comment.addAttribute(StyleConstants.Foreground, Color.GRAY);

		malformed = addStyle("malformed", null);
		malformed.addAttribute(StyleConstants.Foreground, Color.RED);

		assignment = addStyle("assignment", null);
		assignment.addAttribute(StyleConstants.Foreground, Color.BLACK);

		invalidPath = addStyle("invalidPath", null);
		invalidPath.addAttribute(StyleConstants.Background, Color.RED);

		redefinedAttribute = addStyle("redefinedAttribute", null);
		redefinedAttribute.addAttribute(StyleConstants.Background, Color.YELLOW);

	}

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offs, str, a);
		updateLines(offs, str.length());
	}

	@Override
	public void remove(int offset, int length) throws BadLocationException {
		super.remove(offset, length);
		updateLines(offset, 0);
	}

	/**
	 * Load all properties and the corresponding files that they are defined in
	 */
	private void loadConfiguration() {
		Config config = ShellManager.getManager().getConfig();
		// Clear currently loaded properties
		loadedProperties = new LinkedHashMap<Properties, Object>();

		for (Object source : config.getSources()) {
			Properties props = new Properties();
			InputStream is = null;
			try {
				if (source instanceof File) {
					is = new FileInputStream((File) source);
				} else if (source instanceof URL) {
					is = ((URL) source).openStream();
				}
			} catch (FileNotFoundException fnfe) {
				ShellManager.getLogger().log(Level.SEVERE, null, fnfe);
			} catch (IOException ioe) {
				ShellManager.getLogger().log(Level.SEVERE, null, ioe);
			}

			try {
				props.load(is);
				loadedProperties.put(props, source);
			} catch (IOException ex) {
				ShellManager.getLogger().log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Checks a portion of a document's text to see what syntax highlighting should
	 * be applied.
	 * 
	 * @param offset which position in the document to start looking
	 * @param length if the length of text that need be checked.
	 * @throws BadLocationException
	 */
	private void updateLines(int offset, int length) throws BadLocationException {
		errorField.setText("");

		int startLine = getDefaultRootElement().getElementIndex(offset);
		int endLine = getDefaultRootElement().getElementIndex(offset + length);

		for (int line = startLine; line <= endLine; line++) {
			colorLine(line);
		}

	}

	/**
	 * If a given property is being overridden, highlight it and update
	 * the error message to reflect in which files it is also defined
	 * @param property name of the property to validate
	 * @param startOffset the beginning offset of the property text 
	 * within the document
	 */
	private void validateProperty(String property, int startOffset) {

		// Maintain a list of overridden properties
		ArrayList<String> overriddenPropertiesErrors = new ArrayList<String>();
		
		for (Properties prop : loadedProperties.keySet()) {
			if (prop.containsKey(property)) {

				// Determine locations of the properties
				String preExistingPropertyPath = loadedProperties.get(prop).toString();
				String currentPropertyPath = ShellManager.getManager().getConfig().getProperty("jpf.app");

				if (!preExistingPropertyPath.equals(currentPropertyPath)) {

					overriddenPropertiesErrors.add(
							"Warning:" + property + " is overriding " + preExistingPropertyPath
					);

					// Highlight the property being redefined
					setCharacterAttributes(startOffset, property.length(), redefinedAttribute, true);
				}
			}
		}

		errorField.setText(buildErrorMessage(overriddenPropertiesErrors));
	}

	/**
	 * Validate that a given path exists, and highlight the path if it doesn't
	 * @param path The path to validate
	 * @param startOffset The beginning offset of the text
	 */
	private void validatePath(String path, int startOffset) {
		String expandedPath = expandSystemProperty(path);
		if (!pathExists(path)) {
			errorField.setText("Invalid path:" + expandedPath);
			setCharacterAttributes(startOffset, path.length(), invalidPath, true);
		}
	}

	/**
	 * Sets an entire line number to the correct color that it should be.
	 * 
	 * @param lineNum
	 * @throws BadLocationException
	 */
	private void colorLine(int lineNum) throws BadLocationException {
		int startOffset = getDefaultRootElement().getElement(lineNum).getStartOffset();
		int endOffset = getDefaultRootElement().getElement(lineNum).getEndOffset() - 1;

		String line = getText(startOffset, endOffset - startOffset);

		// First check if this uses the "+=" assignment operator
		int assignIndex = line.indexOf("+=");
		int assignLength = 2;

		if (assignIndex == -1) {// += was not found, its probably an '=' assignment
			assignIndex = line.indexOf('=');
			assignLength = 1;
		}

		if (isAssignment(line)) {// We have a key/value pair here
			
			// style the key
			setCharacterAttributes(startOffset, assignIndex, key, true);
			// style the value
			int valueOffset = startOffset + assignIndex + assignLength;
			setCharacterAttributes(valueOffset, line.length() - (assignIndex + assignLength), value, true);

			// Don't forget to style the '=' sign
			setCharacterAttributes(startOffset + assignIndex, assignLength, assignment, true);
			
			// Break each property into its key and values
			String key = (String) line.subSequence(0, line.indexOf('='));
			String assignment = (String) line.subSequence(line.indexOf('=') + 1, line.length());
			String[] values = getAllValuesFromAssignment(assignment);
			
			
			// Ignore @using attributes
			if (key.contains("@"))
				return;

			// Highlight invalid properties
			validateProperty(key, startOffset);

			for (String value : values) {
				// Highlight invalid paths
				if (isPath(value)) {
					// Determine where the current path begins in the document
					int valueStartOffset = startOffset + line.indexOf(assignment) + assignment.indexOf(value);
					validatePath(value, valueStartOffset);
				}
			}
			

		} else {// This line isn't a comment or an assignment, it's probably malformed
			setCharacterAttributes(startOffset, endOffset - startOffset + 1, malformed, true);
		}

		// And now after all of that, check to see if this has a comment in it
		int commentIndex = line.indexOf("#");
		if (commentIndex != -1)
			setCharacterAttributes(startOffset + commentIndex, endOffset - (startOffset + commentIndex), comment, true);

	}

}