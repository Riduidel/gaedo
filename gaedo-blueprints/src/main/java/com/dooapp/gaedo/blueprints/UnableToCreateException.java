package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.Vertex;

public class UnableToCreateException extends BluePrintsCrudServiceException {

	public static UnableToCreateException dueTo(Vertex key, String effectiveType, Exception e) {
		return new UnableToCreateException("unable to create an instance of type "+effectiveType+" for vertex "+GraphUtils.toString(key), e);
	}

	public UnableToCreateException() {
		super();
	}

	public UnableToCreateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToCreateException(String message) {
		super(message);
	}

	public UnableToCreateException(Throwable cause) {
		super(cause);
	}

}
