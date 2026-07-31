package de.unimuenster.imi.randimi.exceptions;

/**
 * Exception that may occur in Controllers.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class ControllerException extends Exception {

	public ControllerException(String message) {
		super(message);
	}

	public static class CSVFileParseException extends ControllerException {
		
		public CSVFileParseException(String csvErrorMessageCode) {
			super(csvErrorMessageCode);
		}
	}
}
