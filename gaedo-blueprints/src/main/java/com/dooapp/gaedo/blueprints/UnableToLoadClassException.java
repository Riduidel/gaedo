package com.dooapp.gaedo.blueprints;

public class UnableToLoadClassException extends BluePrintsCrudServiceException {

	public UnableToLoadClassException() {
	}

	public UnableToLoadClassException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToLoadClassException(String message) {
		super(message);
	}

	public UnableToLoadClassException(Throwable cause) {
		super(cause);
	}

}
