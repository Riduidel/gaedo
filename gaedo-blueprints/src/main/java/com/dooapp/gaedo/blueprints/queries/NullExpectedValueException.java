package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.CrudServiceException;

public class NullExpectedValueException extends CrudServiceException {

	public NullExpectedValueException() {
	}

	public NullExpectedValueException(String message) {
		super(message);
	}

	public NullExpectedValueException(Throwable cause) {
		super(cause);
	}

	public NullExpectedValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
