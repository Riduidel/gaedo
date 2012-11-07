package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.CrudServiceException;

public abstract class UnableToLocateInformerForException extends CrudServiceException {

	public UnableToLocateInformerForException() {
		super();
	}

	public UnableToLocateInformerForException(String message) {
		super(message);
	}

	public UnableToLocateInformerForException(Throwable cause) {
		super(cause);
	}

	public UnableToLocateInformerForException(String message, Throwable cause) {
		super(message, cause);
	}

}