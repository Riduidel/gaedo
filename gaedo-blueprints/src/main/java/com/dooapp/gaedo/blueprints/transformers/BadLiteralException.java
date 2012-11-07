package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class BadLiteralException extends BluePrintsCrudServiceException {

	public BadLiteralException() {
		super();
	}

	public BadLiteralException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadLiteralException(String message) {
		super(message);
	}

	public BadLiteralException(Throwable cause) {
		super(cause);
	}

}
