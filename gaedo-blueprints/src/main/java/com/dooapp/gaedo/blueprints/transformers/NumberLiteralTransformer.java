package com.dooapp.gaedo.blueprints.transformers;

import java.util.Map;
import java.util.TreeMap;

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

	/**
	 * An optimizer to solve https://github.com/Riduidel/gaedo/issues/70
	 */
	private static Map<String, Class> lookupOptimizer = new TreeMap<String, Class>();

	static {
		for(Class<?> clazz : NUMBER_CLASSES) {
			lookupOptimizer.put(clazz.getName(), clazz);
		}
	}

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
		Class<?> used = lookupOptimizer.get(effectiveType);
		if(used==null)
			return effectiveType;
		if(used.isPrimitive())
			return Utils.maybeObjectify(used).getName();
		return effectiveType;
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return lookupOptimizer.containsKey(effectiveType);
	}
}
