package com.dooapp.gaedo.blueprints.strategies;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class BeanIsNotAPropertyBagException extends BluePrintsCrudServiceException {

	public BeanIsNotAPropertyBagException() {
	}

	public BeanIsNotAPropertyBagException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeanIsNotAPropertyBagException(String message) {
		super(message);
	}

	public BeanIsNotAPropertyBagException(Throwable cause) {
		super(cause);
	}

}
