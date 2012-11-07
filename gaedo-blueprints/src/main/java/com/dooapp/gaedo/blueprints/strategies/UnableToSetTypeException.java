package com.dooapp.gaedo.blueprints.strategies;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class UnableToSetTypeException extends BluePrintsCrudServiceException {

	public UnableToSetTypeException() {
	}

	public UnableToSetTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToSetTypeException(String message) {
		super(message);
	}

	public UnableToSetTypeException(Throwable cause) {
		super(cause);
	}

}
