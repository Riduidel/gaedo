package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.CrudServiceException;

public abstract class BluePrintsCrudServiceException extends CrudServiceException {

	public BluePrintsCrudServiceException() {
		super();
	}

	public BluePrintsCrudServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public BluePrintsCrudServiceException(String message) {
		super(message);
	}

	public BluePrintsCrudServiceException(Throwable cause) {
		super(cause);
	}

}
