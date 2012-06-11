package com.dooapp.gaedo.exceptions;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.QueryStatement;

/**
 * Exception thrown when a range, defined for the {@link QueryStatement#get(int, int)} is incorrect
 * @author ndx
 *
 */
public class BadRangeException extends CrudServiceException {

	public BadRangeException() {
	}

	public BadRangeException(String message) {
		super(message);
	}

	public BadRangeException(Throwable cause) {
		super(cause);
	}

	public BadRangeException(String message, Throwable cause) {
		super(message, cause);
	}

}
