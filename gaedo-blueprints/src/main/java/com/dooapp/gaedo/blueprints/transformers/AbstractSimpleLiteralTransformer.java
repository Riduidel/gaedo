package com.dooapp.gaedo.blueprints.transformers;

public abstract class AbstractSimpleLiteralTransformer<Type> extends AbstractLiteralTransformer<Type>{
	protected final Class<Type> type;
	private final String typeName;

	public AbstractSimpleLiteralTransformer(Class<Type> type) {
		super();
		this.type = type;
		this.typeName = type.getName();
	}


	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return typeName.equals(effectiveType);
	}

	public boolean areEquals(Object expected, String effectiveGraphValue) {
		if(type.isAssignableFrom(expected.getClass()))
			return toString(type.cast(expected)).equals(effectiveGraphValue);
		return false;
	}
}
