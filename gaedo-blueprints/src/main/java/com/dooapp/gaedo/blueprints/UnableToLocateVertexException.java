package com.dooapp.gaedo.blueprints;

public class UnableToLocateVertexException extends BluePrintsCrudServiceException {

	public UnableToLocateVertexException(String property, Object value) {
		super("unable to find any vertex having for property \""+property+"\" the value \""+value+"\"");
	}

}
