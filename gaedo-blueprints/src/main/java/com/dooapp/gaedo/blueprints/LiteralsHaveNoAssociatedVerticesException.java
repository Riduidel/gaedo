package com.dooapp.gaedo.blueprints;

public abstract class LiteralsHaveNoAssociatedVerticesException extends BluePrintsCrudServiceException {

	private static final String MESSAGE_PREFIX = "Due to addition of properties to graph entities, there is no more justification to having vertices for literals. See https://github.com/Riduidel/gaedo/issues/68 for more details.\n";

	public LiteralsHaveNoAssociatedVerticesException() {
		super(MESSAGE_PREFIX);
	}

	public LiteralsHaveNoAssociatedVerticesException(String message, Throwable cause) {
		super(MESSAGE_PREFIX+message, cause);
	}

	public LiteralsHaveNoAssociatedVerticesException(String message) {
		super(MESSAGE_PREFIX+message);
	}

	public LiteralsHaveNoAssociatedVerticesException(Throwable cause) {
		super(cause);
	}

}
