package com.dooapp.gaedo.blueprints;

/**
 * Thrown when vertex has not the expected property
 * @author ndx
 *
 */
public class VertexHasNoPropertyException extends BluePrintsCrudServiceException {

	public VertexHasNoPropertyException() {
	}

	public VertexHasNoPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public VertexHasNoPropertyException(String message) {
		super(message);
	}

	public VertexHasNoPropertyException(Throwable cause) {
		super(cause);
	}

}
