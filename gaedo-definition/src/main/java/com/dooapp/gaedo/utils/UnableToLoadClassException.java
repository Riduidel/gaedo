package com.dooapp.gaedo.utils;

import com.dooapp.gaedo.CrudServiceException;

public class UnableToLoadClassException extends CrudServiceException {

	public UnableToLoadClassException(String value, ClassLoader[] used) {
		super("unable to load class "+value+" with any of the given classloaders\n"+used);
	}

}
