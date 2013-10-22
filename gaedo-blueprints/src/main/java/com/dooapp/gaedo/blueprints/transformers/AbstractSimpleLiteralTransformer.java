package com.dooapp.gaedo.blueprints.transformers;

public abstract class AbstractSimpleLiteralTransformer<Type> extends AbstractLiteralTransformer<Type>{
	private final Class type;
	private final String typeName;

	public AbstractSimpleLiteralTransformer(Class<?> type) {
		super();
		this.type = type;
		this.typeName = type.getName();
	}


	@Override
	protected Class getValueClass(Type value) {
		return type;
	}

	@Override
	protected String resolveType(String effectiveType) {
		return typeName;
	}

	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return typeName.equals(effectiveType);
	}
}
