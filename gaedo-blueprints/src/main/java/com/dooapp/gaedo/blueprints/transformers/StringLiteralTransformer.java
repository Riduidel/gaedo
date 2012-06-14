package com.dooapp.gaedo.blueprints.transformers;


public class StringLiteralTransformer extends AbstractLiteralTransformer<String> implements LiteralTransformer<String> {

	@Override
	protected Object getVertexValue(String value) {
		return value==null ? "null" : value;
	}

	@Override
	protected Class getValueClass(String value) {
		return String.class;
	}
}
