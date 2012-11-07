package com.dooapp.gaedo.utils;

import com.dooapp.gaedo.CrudServiceException;

public class UnableToTranscodeNumberException extends CrudServiceException {

	public UnableToTranscodeNumberException() {
	}

	public UnableToTranscodeNumberException(String message) {
		super(message);
	}

	public UnableToTranscodeNumberException(Throwable cause) {
		super(cause);
	}

	public UnableToTranscodeNumberException(String message, Throwable cause) {
		super(message, cause);
	}

}
