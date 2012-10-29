package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class UnableToStoreSerializableException extends BluePrintsCrudServiceException {

	public UnableToStoreSerializableException(String message, Throwable cause) {
		super(message, cause);
	}

}
