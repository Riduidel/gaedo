package com.dooapp.gaedo.properties;

import com.dooapp.gaedo.CrudServiceException;

public class UnusableTypeException extends CrudServiceException {

	public UnusableTypeException() {
	}

	public UnusableTypeException(String message) {
		super(message);
	}

	public UnusableTypeException(Throwable cause) {
		super(cause);
	}

	public UnusableTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
