package com.dooapp.gaedo.blueprints;

/**
 * Exception used when a collection SHOULD be ordered, but not all the Edges have an index assigned!
 */
public class UnableToSortException
		extends BluePrintsCrudServiceException {

	public UnableToSortException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
