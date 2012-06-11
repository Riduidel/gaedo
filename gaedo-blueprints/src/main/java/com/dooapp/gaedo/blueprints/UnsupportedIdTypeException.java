package com.dooapp.gaedo.blueprints;

public class UnsupportedIdTypeException extends BluePrintsCrudServiceException {

	public UnsupportedIdTypeException() {
	}

	public UnsupportedIdTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedIdTypeException(String message) {
		super(message);
	}

	public UnsupportedIdTypeException(Throwable cause) {
		super(cause);
	}

}
