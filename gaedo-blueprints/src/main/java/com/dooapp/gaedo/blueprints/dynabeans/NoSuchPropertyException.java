package com.dooapp.gaedo.blueprints.dynabeans;

import java.util.Set;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class NoSuchPropertyException extends BluePrintsCrudServiceException {

	public NoSuchPropertyException(String key, Set<String> properties) {
		super("there is no property named \""+key+"\".\nAvailable properties are "+properties);
	}

}
