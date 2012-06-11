package com.dooapp.gaedo.blueprints.transformers;

public class NumberLiteralTransformer extends AbstractLiteralTransformer<Number> implements LiteralTransformer<Number> {

	@Override
	protected Object getVertexValue(Number value) {
		return value.toString();
	}
}
