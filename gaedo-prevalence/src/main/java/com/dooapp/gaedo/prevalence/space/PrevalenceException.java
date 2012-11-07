package com.dooapp.gaedo.prevalence.space;

import com.dooapp.gaedo.CrudServiceException;

public abstract class PrevalenceException extends CrudServiceException {

	public PrevalenceException() {
		super();
	}

	public PrevalenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PrevalenceException(String message) {
		super(message);
	}

	public PrevalenceException(Throwable cause) {
		super(cause);
	}

}
