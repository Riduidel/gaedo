package com.dooapp.gaedo.blueprints;

public class CantCreateAVertexForALiteralException extends LiteralsHaveNoAssociatedVerticesException {

	public CantCreateAVertexForALiteralException() {
	}

	public CantCreateAVertexForALiteralException(String message, Throwable cause) {
		super(message, cause);
	}

	public CantCreateAVertexForALiteralException(String message) {
		super(message);
	}

	public CantCreateAVertexForALiteralException(Throwable cause) {
		super(cause);
	}

}
