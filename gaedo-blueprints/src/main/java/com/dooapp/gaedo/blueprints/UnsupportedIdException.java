package com.dooapp.gaedo.blueprints;

public class UnsupportedIdException extends BluePrintsCrudServiceException {

	public UnsupportedIdException(Class<?> class1, Class<?> type) {
		super("provided id is of type "+class1.getCanonicalName()+" when this service expects a "+type.getCanonicalName());
	}

}
