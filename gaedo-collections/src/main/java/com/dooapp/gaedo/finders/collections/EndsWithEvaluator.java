package com.dooapp.gaedo.finders.collections;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;

public class EndsWithEvaluator<DataType> extends AbstractBasicEvaluator<DataType>
		implements Evaluator<DataType> {

	private String end;

	public EndsWithEvaluator(Property fieldName, String end) {
		super(fieldName);
		this.end = end;
	}

	@Override
	public boolean matches(DataType element) {
		String value = (String) getValue(element);
		return value.endsWith(end);
	}

}
