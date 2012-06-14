package com.dooapp.gaedo.blueprints.transformers;

public class NumberLiteralTransformer extends AbstractLiteralTransformer<Number> implements LiteralTransformer<Number> {

	@Override
	protected Object getVertexValue(Number value) {
		return value==null ? "0" : value.toString();
	}

	@Override
	protected Class getValueClass(Number value) {
		return value==null ? Number.class : value.getClass();
	}
}
