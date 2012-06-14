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

}
