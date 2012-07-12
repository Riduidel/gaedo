package com.dooapp.gaedo.blueprints.transformers;

public class EnumLiteralTransformer extends AbstractLiteralTransformer<Enum> implements LiteralTransformer<Enum> {

	@Override
	protected Object getVertexValue(Enum value) {
		return value.name();
	}

	@Override
	protected Class getValueClass(Enum value) {
		return value==null ? Enum.class : value.getClass();
	}

	@Override
	protected String resolveType(String effectiveType) {
		return effectiveType;
	}

	@Override
	public boolean canHandle(String effectiveType) {
		try {
			return Enum.class.isAssignableFrom(Class.forName(effectiveType));
		} catch (ClassNotFoundException e) {
			throw new UnsupportedOperationException("class "+effectiveType+" isn't considered to be an enum ... Maybe is there any classloader issue hidden there", e);
		}
	}

}
