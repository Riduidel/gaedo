package com.dooapp.gaedo.blueprints;

public class UnableToCreateException extends BluePrintsCrudServiceException {

	public UnableToCreateException(String effectiveType, Exception e) {
		super("unable to create an instance of type "+effectiveType, e);
	}

}
