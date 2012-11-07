package com.dooapp.gaedo.finders.collections;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;

public class ContainsStringEvaluator<DataType> extends AbstractBasicEvaluator<DataType>
		implements Evaluator<DataType> {

	private String contained;

	public ContainsStringEvaluator(Property fieldName, String contained) {
		super(fieldName);
		this.contained = contained;
	}

	@Override
	public boolean matches(DataType element) {
		String value = (String) getValue(element);
		return value.contains(contained);
	}
}
