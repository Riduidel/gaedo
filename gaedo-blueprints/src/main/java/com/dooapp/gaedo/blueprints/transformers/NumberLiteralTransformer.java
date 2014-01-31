package com.dooapp.gaedo.blueprints.transformers;

import java.util.Map;
import java.util.TreeMap;

import com.dooapp.gaedo.utils.PrimitiveUtils;
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
	public String valueToString(Number value) {
		return value==null ? "0" : value.toString();
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return lookupOptimizer.containsKey(effectiveType);
	}

	@Override
	public boolean areEquals(Object expected, String effectiveGraphValue) {
		if(expected instanceof Number) {
			Number expectedNumber = (Number) expected;
			Number value = loadValueFromString(Double.class, LiteralHelper.getValueIn(effectiveGraphValue));
			expectedNumber = PrimitiveUtils.as(expectedNumber, Double.class);
			return expectedNumber.doubleValue()==value.doubleValue();
		} else {
			return false;
		}
	}
}
