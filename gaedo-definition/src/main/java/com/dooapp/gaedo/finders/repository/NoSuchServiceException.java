package com.dooapp.gaedo.finders.repository;

import com.dooapp.gaedo.CrudServiceException;

public class NoSuchServiceException extends CrudServiceException {

	public NoSuchServiceException(String string) {
		super(string);
	}

}
