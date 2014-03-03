package com.dooapp.gaedo.blueprints;

public class ObjectIsNotARealLiteralException extends BluePrintsCrudServiceException {

	public ObjectIsNotARealLiteralException(Object value, Class<? extends Object> valueClass) {
		super("one thought "+value+" could be stored as a literal. However, it is a "+valueClass.getName() + " which is by no means a compatible literal");
	}

}
