package com.dooapp.gaedo.blueprints.transformers;

public abstract class AbstractSimpleLiteralTransformer<Type> extends AbstractLiteralTransformer<Type>{
	private final Class type;

	public AbstractSimpleLiteralTransformer(Class<?> type) {
		super();
		this.type = type;
	}


	@Override
	protected Class getValueClass(Type value) {
		return type;
	}

	@Override
	protected String resolveType(String effectiveType) {
		return type.getName();
	}

	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return type.getName().equals(effectiveType);
	}
}
