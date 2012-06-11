package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.utils.VirtualMethodCreationException;

/**
 * Base class for exceptions concerning matching between declaration and executed code
 * @author ndx
 *
 */
public class CantMatchMethodToCodeException extends
		VirtualMethodCreationException {

	public CantMatchMethodToCodeException() {
		super();
	}

	public CantMatchMethodToCodeException(String message) {
		super(message);
	}

	public CantMatchMethodToCodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CantMatchMethodToCodeException(Throwable cause) {
		super(cause);
	}

}