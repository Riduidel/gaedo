package com.dooapp.gaedo.finders.collections;

import com.dooapp.gaedo.properties.Property;

public class InstanceOfEvaluator<DataType> extends AbstractBasicEvaluator<DataType> implements Evaluator<DataType> {

	private Class<?> type;

	public InstanceOfEvaluator(Property field, Class<?> type) {
		super(field);
		this.type = type;
	}

	@Override
	public boolean matches(DataType element) {
		return type.isInstance(element);
	}

}
