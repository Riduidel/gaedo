package com.dooapp.gaedo.prevalence.space.basic;

import com.dooapp.gaedo.prevalence.space.PrevalenceException;

/**
 * Exception thrown when, due to a command exception, space is no more consistent
 * @author ndx
 *
 */
public class ConsistencyException extends PrevalenceException {

	public ConsistencyException() {
		super();
	}

	public ConsistencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConsistencyException(String message) {
		super(message);
	}

	public ConsistencyException(Throwable cause) {
		super(cause);
	}

}