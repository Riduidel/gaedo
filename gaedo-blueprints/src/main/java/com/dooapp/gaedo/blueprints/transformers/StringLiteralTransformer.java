package com.dooapp.gaedo.blueprints.transformers;

import com.tinkerpop.blueprints.pgm.Vertex;


public class StringLiteralTransformer extends AbstractSimpleLiteralTransformer<String> implements LiteralTransformer<String> {
	public StringLiteralTransformer() {
		super(String.class);
	}

	@Override
	protected Object getVertexValue(String value) {
		return value==null ? "null" : value;
	}

	public boolean doesVertexEndsWith(Vertex currentVertex, String expected) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+StringLiteralTransformer.class.getName()+"#doesVertexEndsWith has not yet been implemented AT ALL");
	}
}
