package com.dooapp.gaedo.google.datastore.hierarchy;

import java.util.Collection;

import com.dooapp.gaedo.google.datastore.GAECrudServiceException;

public class ImpossibleToCreateHierarchyManagerException extends GAECrudServiceException {

	public ImpossibleToCreateHierarchyManagerException(Class<?> containedClass,
			Collection<String> errors) {
		super(
				"due to the following errors, it is impossible to create a hierarchy manager for class "
						+ containedClass.getName() + "\n" + listErrors(errors));
	}

}
