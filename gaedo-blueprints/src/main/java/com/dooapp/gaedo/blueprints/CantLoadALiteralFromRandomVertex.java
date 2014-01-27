package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.Vertex;

public class CantLoadALiteralFromRandomVertex extends LiteralsHaveNoAssociatedVerticesException {

	public CantLoadALiteralFromRandomVertex() {
	}

	public CantLoadALiteralFromRandomVertex(String message, Throwable cause) {
		super(message, cause);
	}

	public CantLoadALiteralFromRandomVertex(String message) {
		super(message);
	}

	public CantLoadALiteralFromRandomVertex(Throwable cause) {
		super(cause);
	}

	public CantLoadALiteralFromRandomVertex(Vertex key) {
		this("It is not possible to load a literal from a random vertex without knowing in which property it is stored\n"
						+ "Loading was attempted from "+GraphUtils.toString(key));
	}

}
