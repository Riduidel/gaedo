package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.CrudServiceException;
import com.google.appengine.api.datastore.Key;

/**
 * Exception thrown when an incompatible key is used for a key-based query
 * @author ndx
 *
 */
public class IncompatibleKeyException extends GAECrudServiceException {

	public IncompatibleKeyException(Key inputKey, String expectedKind) {
		super("unable to find parent key of kind "+expectedKind+" in key "+inputKey);
	}

}
