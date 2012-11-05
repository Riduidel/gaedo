package com.dooapp.gaedo.blueprints.strategies.graph;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

/**
 * Exception thrown if no edge was found in used named graphs
 * @author ndx
 *
 */
public class NoEdgeInNamedGraphsException extends BluePrintsCrudServiceException {

	public NoEdgeInNamedGraphsException() {
	}

	public NoEdgeInNamedGraphsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoEdgeInNamedGraphsException(String message) {
		super(message);
	}

	public NoEdgeInNamedGraphsException(Throwable cause) {
		super(cause);
	}

}
