package com.dooapp.gaedo.blueprints.transformers;


public class ClassLiteralTransformer extends AbstractLiteralTransformer<Class> implements LiteralTransformer<Class> {
	@Override
	protected Object getVertexValue(Class value) {
		return value==null ? Object.class.getCanonicalName() : value.getCanonicalName();
	}

	@Override
	protected Class getValueClass(Class value) {
		return Class.class;
	}

}
