package com.dooapp.gaedo.utils;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Base class for all exceptions regarding invocation handlers virtual methods creation
 * @author ndx
 *
 */
public class VirtualMethodCreationException extends CrudServiceException {

	public VirtualMethodCreationException() {
	}

	public VirtualMethodCreationException(String message) {
		super(message);
	}

	public VirtualMethodCreationException(Throwable cause) {
		super(cause);
	}

	public VirtualMethodCreationException(String message, Throwable cause) {
		super(message, cause);
	}

}
