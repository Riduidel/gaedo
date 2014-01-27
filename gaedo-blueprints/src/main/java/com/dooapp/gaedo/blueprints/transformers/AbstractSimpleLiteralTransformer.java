package com.dooapp.gaedo.blueprints.transformers;

public abstract class AbstractSimpleLiteralTransformer<Type> extends AbstractLiteralTransformer<Type>{
	protected final Class<Type> type;
	private final String typeName;

	public AbstractSimpleLiteralTransformer(Class<Type> type) {
		super();
		this.type = type;
		this.typeName = type.getName();
	}


	@Override
	protected String resolveType(String effectiveType) {
		return typeName;
	}

	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return typeName.equals(effectiveType);
	}

	@Override
	protected String typeToString(Class<? extends Type> valueClass) {
		return typeName;
	}

	public boolean areEquals(Object expected, String effectiveGraphValue) {
		if(type.isAssignableFrom(expected.getClass()))
			return toString(type.cast(expected)).equals(effectiveGraphValue);
		return false;
	}
}
