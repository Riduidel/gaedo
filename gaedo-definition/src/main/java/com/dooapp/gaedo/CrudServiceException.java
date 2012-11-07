package com.dooapp.gaedo;

import java.util.Collection;

/**
 * Base class for all service exception. It's a runtime one for simpler code, and to allow it to break all the thread.
 * @author ndx
 *
 */
public class CrudServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a string listing error messages from an error message list (a better toString, in a sense)
	 * @param errors
	 * @return
	 */
	public static Object listErrors(Collection<String> errors) {
		StringBuilder sOut = new StringBuilder();
		for(String s : errors) {
			sOut.append("\t").append(s).append("\n");
		}
		return sOut.toString();
	}

	public CrudServiceException() {
	}

	public CrudServiceException(String message) {
		super(message);
	}

	public CrudServiceException(Throwable cause) {
		super(cause);
	}

	public CrudServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
