package com.dooapp.gaedo.blueprints.indexable;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class UnsupportedElementTypeException extends BluePrintsCrudServiceException {

	public UnsupportedElementTypeException() {
	}

	public UnsupportedElementTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedElementTypeException(String message) {
		super(message);
	}

	public UnsupportedElementTypeException(Throwable cause) {
		super(cause);
	}

}
