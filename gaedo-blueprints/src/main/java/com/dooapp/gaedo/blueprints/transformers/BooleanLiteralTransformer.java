package com.dooapp.gaedo.blueprints.transformers;


public class BooleanLiteralTransformer extends AbstractSimpleLiteralTransformer<Boolean> implements LiteralTransformer<Boolean> {
	public BooleanLiteralTransformer() {
		super(Boolean.class);
	}

	@Override
	protected Object getVertexValue(Boolean value) {
		return value==null ? Boolean.FALSE.toString() : value.toString();
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return Boolean.TYPE.getName().equals(effectiveType) || Boolean.class.getName().equals(effectiveType);
	}
}
