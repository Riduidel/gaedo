package com.dooapp.gaedo.properties;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Base exception class for property providers.
 * @author ndx
 *
 */
public abstract class PropertyProviderException extends CrudServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PropertyProviderException() {
	}

	public PropertyProviderException(String message) {
		super(message);
	}

	public PropertyProviderException(Throwable cause) {
		super(cause);
	}

	public PropertyProviderException(String message, Throwable cause) {
		super(message, cause);
	}

}
