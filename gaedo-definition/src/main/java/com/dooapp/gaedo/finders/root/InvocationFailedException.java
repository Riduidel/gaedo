package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Embedding exception for all invokation issues
 * 
 * @author Nicolas
 * 
 */
public class InvocationFailedException extends CrudServiceException {

	public InvocationFailedException(Exception e) {
		super(e);
	}

}