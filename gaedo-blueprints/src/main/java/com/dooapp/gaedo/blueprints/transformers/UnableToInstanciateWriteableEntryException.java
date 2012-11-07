package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class UnableToInstanciateWriteableEntryException extends BluePrintsCrudServiceException {

	public UnableToInstanciateWriteableEntryException(Exception e) {
		super(e);
	}

}
