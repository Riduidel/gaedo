package com.dooapp.gaedo.google.datastore;

import java.util.Arrays;

public class BadIdArgumentException extends GAECrudServiceException {

	public BadIdArgumentException(Object[] id) {
		super("in GAE, id is stored on one long field not on "+id.length+" sized array containing "+Arrays.toString(id));
	}

}
