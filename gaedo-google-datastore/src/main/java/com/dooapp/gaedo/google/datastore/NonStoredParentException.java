package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.properties.Property;

public class NonStoredParentException extends GAECrudServiceException {

	public NonStoredParentException(Property parentField) {
		super(
				"Class "
						+ parentField.getDeclaringClass()
						+ " declares field "
						+ parentField
						+ " as parent, but it is not persisted using GAE datastore. As a consequence, we can't access it to build a key.");
	}

}
