package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.utils.Utils;

public class NumberLiteralTransformer extends AbstractLiteralTransformer<Number> implements LiteralTransformer<Number> {
	private static final Class<?>[] NUMBER_CLASSES = new Class<?> [] {
		Short.class,
		Byte.class,
		Number.class,
		Integer.class,
		Float.class,
		Long.class,
		Double.class,
		Short.TYPE,
		Byte.TYPE,
		Integer.TYPE,
		Float.TYPE,
		Long.TYPE,
		Double.TYPE
	};

	@Override
	protected Object getVertexValue(Number value) {
		return value==null ? "0" : value.toString();
	}

	@Override
	protected Class getValueClass(Number value) {
		return value==null ? Number.class : value.getClass();
	}

	/**
	 * This method is heavy as hell, but necessary
	 * @param effectiveType
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractLiteralTransformer#resolveType(java.lang.String)
	 */
	@Override
	protected String resolveType(String effectiveType) {
		for(Class<?> c : NUMBER_CLASSES) {
			if(c.getName().equals(effectiveType))  {	
				if(c.isPrimitive()) {
					return Utils.maybeObjectify(c).getName();
				}
			}
		}
		return effectiveType;
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		for(Class<?> c : NUMBER_CLASSES) {
			if(c.getName().equals(effectiveType))
				return true;
		}
		return false;
	}
}
