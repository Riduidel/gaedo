package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.Properties;
import com.tinkerpop.blueprints.pgm.Vertex;


public class StringLiteralTransformer extends AbstractSimpleLiteralTransformer<String> implements LiteralTransformer<String> {
	public StringLiteralTransformer() {
		super(String.class);
	}

	@Override
	protected Object getVertexValue(String value) {
		return value==null ? "null" : value;
	}
}
