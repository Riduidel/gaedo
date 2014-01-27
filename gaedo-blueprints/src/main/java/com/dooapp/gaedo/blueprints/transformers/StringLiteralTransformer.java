package com.dooapp.gaedo.blueprints.transformers;



public class StringLiteralTransformer extends AbstractSimpleLiteralTransformer<String> implements LiteralTransformer<String> {
	public StringLiteralTransformer() {
		super(String.class);
	}

	@Override
	public String valueToString(String value) {
		return value==null ? "null" : value;
	}
}