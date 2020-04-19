package jpf.shell.util.editor;

import java.util.ArrayList;
import java.util.Properties;
import static jpf.shell.util.editor.Utilities.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UtilitiesTest {
	
	@Nested
	class expandSystemProperty {
		
		final String SYSTEM_PROPERTY_KEY = "shell.test";
		final String SYSTEM_PROPERTY_VALUE = "expanded";
				
		@BeforeEach
		public void initSystemProperties() {
			// Set a test specific system property for validation
			Properties props = System.getProperties();
			props.setProperty(SYSTEM_PROPERTY_KEY, SYSTEM_PROPERTY_VALUE);
		}
		
		/**
		 * A valid system property is expected to be expanded
		 * in place
		 */
		@Test
		public void validProperty() {
			String expandedProperty = expandSystemProperty(
					String.format("${%s}", SYSTEM_PROPERTY_KEY)
			);
			assertEquals(SYSTEM_PROPERTY_VALUE, expandedProperty);
		}
		
		/**
		 * A string with no system properties is expected to 
		 * remain the same after being expanded.
		 */
		@Test
		public void noPropertyInString() {
			String noSystemProperty = "none";
			assertEquals(noSystemProperty, expandSystemProperty(noSystemProperty));
			
			// Check with varying key characters
			noSystemProperty = "${missingRightCurlyBrace";
			assertEquals(noSystemProperty, expandSystemProperty(noSystemProperty));
			noSystemProperty = "$missingLeftCurlyBrace}";
			assertEquals(noSystemProperty, expandSystemProperty(noSystemProperty));
			noSystemProperty = "{missingDollarSign";
			assertEquals(noSystemProperty, expandSystemProperty(noSystemProperty));
			noSystemProperty = "$}backwards{$";
			assertEquals(noSystemProperty, expandSystemProperty(noSystemProperty));
			noSystemProperty = "${{nestedShouldNotWork}}";
			assertEquals(noSystemProperty, expandSystemProperty(noSystemProperty));
		}
		
		
		/**
		 * A system property which does not exist but still enclosed in 
		 * correct characters is expected to remain the same
		 */
		@Test
		public void propertyDoesNotExist() {
			String nonExistantProperty = "${property.doesnotexist}";
			assertEquals(nonExistantProperty, expandSystemProperty(nonExistantProperty));
		}
		
		/**
		 * An empty system property is expected to remain the same
		 */
		@Test
		public void emptySystemProperty() {
			String emptyProperty = "${}";
			assertEquals(emptyProperty, expandSystemProperty(emptyProperty));
		}
		
		/**
		 * Multiple system properties in a given String are expected
		 * to all be expanded
		 */
		@Test
		public void multipleSystemProperties() {
			String multipleProperties = expandSystemProperty(
				String.format("${%s}${%s}", SYSTEM_PROPERTY_KEY, SYSTEM_PROPERTY_KEY)
			);
			assertEquals(
				SYSTEM_PROPERTY_VALUE + SYSTEM_PROPERTY_VALUE, 
				expandSystemProperty(multipleProperties)
			);
		}	
		
	}
	
	@Nested
	class pathExists {
		
		/**
		 * Expected that current user directory is a valid path
		 */
		@Test
		public void validPath() {
			String validPath = System.getProperty("user.dir");
			assertTrue(pathExists(validPath));
		}

		/**
		 * Expected that current directory is recognized as a path
		 */
		@Test
		public void currentDirectory() {
			String currentDir = "./";
			assertTrue(pathExists(currentDir));
		}
		
		/**
		 * Expected that an invalid path format returns false
		 */
		@Test
		public void invalidPath() {
			String invalidPath = "this\\path /is\\invalid";
			assertFalse(pathExists(invalidPath));
		}
		
		/**
		 * Expected that a non-existent path returns false
		 */
		@Test
		public void pathDoesNotExist() {
			String nonExistantPath = "/this/path/shouldnt/exist";
			assertFalse(pathExists(nonExistantPath));
		}
		
	}
	
	@Nested
	class isPath {
		
		/**
		 * Note that this is not checking if the path exists,
		 * just that it follows the expected format of a path.
		 */
		@Test
		public void validPath() {
			String validPath = System.getProperty("user.dir");
			assertTrue(isPath(validPath));
			validPath = "/this/is/valid/format";
			assertTrue(isPath(validPath));
			validPath = "./";
			assertTrue(isPath(validPath));
		}
		
		/**
		 * Expected that paths not following expected format
		 * return false
		 */
		@Test
		public void invalidPath() {
			String invalidPath = "not.valid.path.format";
			assertFalse(isPath(invalidPath));
			invalidPath = "notValidEither";
			assertFalse(isPath(invalidPath));
			invalidPath = "";
			assertFalse(isPath(invalidPath));
		}
		
	}
	
	@Nested
	class buildErrorMessage {
		
		/**
		 * Expected that multiple errors are appended within
		 * HTML tags separated by line breaks
		 */
		@Test
		public void valid() {
			String expectedErrorMessage = "<HTML>error1<BR>error2</HTML>";
			ArrayList<String> errorList = new ArrayList<String>();
			errorList.add("error1");
			errorList.add("error2");
			assertEquals(expectedErrorMessage, buildErrorMessage(errorList));	
		}
		
		/**
		 * Expected that given no errors, there are no line breaks
		 */
		@Test
		public void empty() {
			String expectedEmptyErrorMessage = "<HTML></HTML>";
			ArrayList<String> errorList = new ArrayList<String>();
			assertEquals(expectedEmptyErrorMessage, buildErrorMessage(errorList));	
		}
		
	}
	
	@Nested
	class isAssignment {
		
		/**
		 * Expected that valid key value pairings return true
		 */
		@Test
		public void valid() {
			String validAssignment = "key=value";
			assertTrue(validAssignment, isAssignment(validAssignment));
			validAssignment = "key+=value";
			assertTrue(validAssignment, isAssignment(validAssignment));
			validAssignment = "key.subkey=value";
			assertTrue(validAssignment, isAssignment(validAssignment));
			validAssignment = "key.subkey+=value";
			assertTrue(validAssignment, isAssignment(validAssignment));
		}
		
		/**
		 * Expected that strings which don't contain one of the key value
		 * assignment operators return false
		 */
		@Test
		public void invalid() {
			String invalidAssignment = "noAssignment";
			assertFalse(invalidAssignment, isAssignment(invalidAssignment));
			invalidAssignment = "";
			assertFalse(invalidAssignment, isAssignment(invalidAssignment));
		}
		
	}
	
	@Nested
	class getAllValuesFromAssignment {
		
		/**
		 * Expected that a single assignment with no delimiters returns
		 * only the value itself 
		 */
		@Test
		public void validSingleValue() {
			String validAssignment = "value";
			String[] expectedValues = new String[] {"value"};
			assertArrayEquals(expectedValues, getAllValuesFromAssignment(validAssignment));
		}
		
		/**
		 * Expected that an assignment separated by multiple delimiters is
		 * returned as an array of each value without the delimiters
		 */
		@Test
		public void validMultipleValues() {
			String validAssignments = "value1,value2,value3";
			//comma
			String[] expectedValues = new String[] {"value1", "value2", "value3"};
			assertArrayEquals(expectedValues, getAllValuesFromAssignment(validAssignments));
			// semicolon
			validAssignments = "value1;value2;value3";
			assertArrayEquals(expectedValues, getAllValuesFromAssignment(validAssignments));
			//backslash
			validAssignments = "value1\\value2\\value3";
			assertArrayEquals(expectedValues, getAllValuesFromAssignment(validAssignments));
			//assortment
			validAssignments = "value1,value2;value3";
			assertArrayEquals(expectedValues, getAllValuesFromAssignment(validAssignments));
		}
		
	}
}



