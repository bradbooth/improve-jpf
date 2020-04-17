package jpf.shell.util.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains utility methods
 */
public class Utilities {
	
	/**
	 * Replace ${variable} in a given string with its corresponding system property
	 * @param value the string which may or may not contain embedded system property within
	 * @return the string with the expanded property
	 */
	public static String expandSystemProperty(String value) {

		String propertyPattern = "\\$\\{(.+?)\\}"; // Matches on ${...}
		Pattern results = Pattern.compile(propertyPattern);
		Matcher matches = results.matcher(value);

		while (matches.find()) {
			String property = matches.group(0).substring(2, matches.group(0).length() - 1);
			// Replace ${...} with corresponding system property
			if (System.getProperty(property) != null)
				value = value.replaceAll(propertyPattern, System.getProperty(property));
		}
		return value;
	}
	

	/**
	 * Validate that a given path exists on the file system
	 * @param path the path to check
	 * @return true if the path exists, otherwise false
	 */
	public static Boolean pathExists(String path) {
		String expandedPath = Utilities.expandSystemProperty(path);
		// Remove trailing & leading whitespace
		expandedPath = expandedPath.trim();
		// Validate the path
		File dir = new File(expandedPath);
		Boolean pathExists = dir.exists();
		return pathExists;
	}
	
	/**
	 * Build an HTML based error message that will span multiple lines for each
	 * error message.
	 * 
	 * @param ErrorMessages - List of error messages
	 * @return
	 */
	public static String buildErrorMessage(ArrayList<String> ErrorMessages) {
		StringBuilder sb = new StringBuilder();
		sb.append("<HTML>");
		sb.append(String.join("<BR>", ErrorMessages));
		sb.append("</HTML>");
		return sb.toString();
	}
	
	/**
	 * Validate if the current value is expected to be a path. Does <b> not </b>
	 * validate that the path exists or is valid.
	 * 
	 * @param value
	 * @return True if value follows path format, otherwise false
	 */
	public static boolean isPath(String value) {
		// Check for both Unix or windows paths
		return value.contains("/") || value.contains("\\");
	}

	/**
	 * Returns true if a given line is an assignment, for example target=testFile or
	 * target+=testFile are both assignments.
	 * 
	 * @param line
	 * @return true if line contains += or =
	 */
	public static boolean isAssignment(String line) {
		// First check if this uses the "+=" assignment operator
		int assignIndex = line.indexOf("+=");
		if (assignIndex == -1) {
			// += was not found, its probably an '=' assignment
			assignIndex = line.indexOf('=');
		}
		return assignIndex > -1;
	}
	
	/**
	 * Properties can be separated by , or ; \
	 * Return a list of these properties after being separated
	 * 
	 * @param value
	 * @return
	 */
	public static String[] getAllValuesFromAssignment(String value) {
		return value.split("(,|;|\\\\)");
	}
}

