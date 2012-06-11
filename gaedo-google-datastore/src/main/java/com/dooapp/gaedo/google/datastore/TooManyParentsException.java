package com.dooapp.gaedo.google.datastore;

import java.util.Collection;

import com.dooapp.gaedo.properties.Property;

public class TooManyParentsException extends GAECrudServiceException {

	public TooManyParentsException(Class<?> containedClass, Collection<Property> parents) {
		super("Class "+containedClass.getName()+" hierarchy must have at max one field annotated with @Parent. Currently, there are "+parents);
	}
}
