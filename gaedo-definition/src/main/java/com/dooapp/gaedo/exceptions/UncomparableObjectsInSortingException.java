package com.dooapp.gaedo.exceptions;

import com.dooapp.gaedo.CrudServiceException;

public class UncomparableObjectsInSortingException extends CrudServiceException {

	public UncomparableObjectsInSortingException() {
	}

	public UncomparableObjectsInSortingException(String message) {
		super(message);
	}

	public UncomparableObjectsInSortingException(Throwable cause) {
		super(cause);
	}

	public UncomparableObjectsInSortingException(String message, Throwable cause) {
		super(message, cause);
	}

}
