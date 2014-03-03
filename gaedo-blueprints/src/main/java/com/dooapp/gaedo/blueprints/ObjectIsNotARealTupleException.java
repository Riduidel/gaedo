package com.dooapp.gaedo.blueprints;

public class ObjectIsNotARealTupleException extends BluePrintsCrudServiceException {

	public ObjectIsNotARealTupleException(Object value, Class<? extends Object> valueClass) {
		super("one thought "+value+" could be stored as a tuple. However, it is a "+valueClass.getName() + " which is by no means a compatible literal");
	}

}
