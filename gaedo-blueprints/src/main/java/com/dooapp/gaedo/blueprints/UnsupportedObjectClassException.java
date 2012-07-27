package com.dooapp.gaedo.blueprints;

public class UnsupportedObjectClassException extends BluePrintsCrudServiceException {

	public UnsupportedObjectClassException() {
	}

	public UnsupportedObjectClassException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedObjectClassException(String message) {
		super(message);
	}

	public UnsupportedObjectClassException(Throwable cause) {
		super(cause);
	}

}
