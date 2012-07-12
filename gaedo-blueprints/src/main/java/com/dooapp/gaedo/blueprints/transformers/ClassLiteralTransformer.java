package com.dooapp.gaedo.blueprints.transformers;


public class ClassLiteralTransformer extends AbstractSimpleLiteralTransformer<Class> implements LiteralTransformer<Class> {
	public ClassLiteralTransformer() {
		super(Class.class);
	}
	@Override
	protected Object getVertexValue(Class value) {
		return value==null ? Object.class.getCanonicalName() : value.getCanonicalName();
	}

	@Override
	protected Class getValueClass(Class value) {
		return Class.class;
	}


	@Override
	protected String resolveType(String effectiveType) {
		return Class.class.getName();
	}
}
