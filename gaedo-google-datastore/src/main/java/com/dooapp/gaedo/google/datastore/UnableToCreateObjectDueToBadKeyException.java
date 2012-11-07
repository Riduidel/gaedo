package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.utils.UnableToCreateObjectException;
import com.google.appengine.api.datastore.Key;

public class UnableToCreateObjectDueToBadKeyException extends UnableToCreateObjectException {

	public UnableToCreateObjectDueToBadKeyException(Key input,
			Class<?> containedClass) {
		super("unable to create object associated with incompatible key "+input+"\nThis service manages objects compatible with class "+containedClass.toString());
	}

}
