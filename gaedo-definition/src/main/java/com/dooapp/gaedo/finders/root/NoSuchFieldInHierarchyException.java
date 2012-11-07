package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.CrudServiceException;

public class NoSuchFieldInHierarchyException extends CrudServiceException {
	public NoSuchFieldInHierarchyException(String fieldName) {
		super("the field named \""+fieldName+"\" does not seems to exist");
	}
	
}