package com.dooapp.gaedo.rest.server;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Exception used in Rest server code are all subclasses of this one.
 * @author ndx
 *
 */
public abstract class RestServerException extends CrudServiceException {

	public RestServerException() {
	}

	public RestServerException(String message) {
		super(message);
	}

	public RestServerException(Throwable cause) {
		super(cause);
	}

	public RestServerException(String message, Throwable cause) {
		super(message, cause);
	}

}
