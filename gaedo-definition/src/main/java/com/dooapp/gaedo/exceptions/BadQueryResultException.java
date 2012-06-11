package com.dooapp.gaedo.exceptions;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Exception thrown when query result is, in any way, an invalid result
 * @author ndx
 *
 */
public class BadQueryResultException extends CrudServiceException {

	public BadQueryResultException() {
	}

	public BadQueryResultException(String message) {
		super(message);
	}

	public BadQueryResultException(Throwable cause) {
		super(cause);
	}

	public BadQueryResultException(String message, Throwable cause) {
		super(message, cause);
	}

}
