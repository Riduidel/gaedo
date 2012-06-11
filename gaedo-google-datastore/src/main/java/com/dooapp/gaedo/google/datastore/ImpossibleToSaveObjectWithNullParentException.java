package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.properties.Property;

public class ImpossibleToSaveObjectWithNullParentException extends
		GAECrudServiceException {

	public ImpossibleToSaveObjectWithNullParentException(Object toCreate,Class<?> containedClass,
			Property parentField) {
		super("you tried to save object "+toCreate+" of class "+containedClass.getClass().getName()+".\n" +
				"This class or its hierarchy defines a parent field "+parentField+" which CANNOT be null when object is saved.");
	}

}
