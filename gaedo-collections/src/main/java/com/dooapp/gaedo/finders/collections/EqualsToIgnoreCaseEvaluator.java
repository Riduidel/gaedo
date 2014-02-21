package com.dooapp.gaedo.finders.collections;

import com.dooapp.gaedo.properties.Property;

public class EqualsToIgnoreCaseEvaluator<DataType> extends AbstractBasicEvaluator<DataType> implements Evaluator<DataType> {

	private String compared;

	public EqualsToIgnoreCaseEvaluator(Property field, String compared) {
		super(field);
		this.compared = compared;
	}

	@Override
	public boolean matches(DataType element) {
		String value = (String) getValue(element);
		return value.equalsIgnoreCase(compared);
	}

}
