package com.dooapp.gaedo.blueprints.strategies;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class UnableToGetVertexTypeException extends BluePrintsCrudServiceException {

	public UnableToGetVertexTypeException() {
	}

	public UnableToGetVertexTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToGetVertexTypeException(String message) {
		super(message);
	}

	public UnableToGetVertexTypeException(Throwable cause) {
		super(cause);
	}

}
