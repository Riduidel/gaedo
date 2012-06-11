package com.dooapp.gaedo.finders.collections;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;

public class StartsWithEvaluator<DataType> extends AbstractBasicEvaluator<DataType>
		implements Evaluator<DataType> {

	private String start;

	public StartsWithEvaluator(Property fieldName, String start) {
		super(fieldName);
		this.start = start;
	}

	@Override
	public boolean matches(DataType element) {
		String value = (String) getValue(element);
		return value.startsWith(start);
	}

}
