package com.dooapp.gaedo.extensions.migrable;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Thrown when a migrator cannot be instanciated
 * @author ndx
 *
 */
public class UnableToCreateMigratorException extends CrudServiceException {

	public UnableToCreateMigratorException() {
		super();
	}

	public UnableToCreateMigratorException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToCreateMigratorException(String message) {
		super(message);
	}

	public UnableToCreateMigratorException(Throwable cause) {
		super(cause);
	}

}
