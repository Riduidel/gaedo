package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Base class for GAE exceptions, providing a kind of central extension point
 * @author ndx
 *
 */
public abstract class GAECrudServiceException extends CrudServiceException {

	public GAECrudServiceException() {
	}

	public GAECrudServiceException(String message) {
		super(message);
	}

	public GAECrudServiceException(Throwable cause) {
		super(cause);
	}

	public GAECrudServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
