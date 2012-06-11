package com.dooapp.gaedo.finders.collections;

import com.dooapp.gaedo.properties.Property;

public class AnythingEvaluator<DataType> extends AbstractBasicEvaluator<DataType> {

	public AnythingEvaluator(Property field) {
		super(field);
	}

	@Override
	public boolean matches(DataType element) {
		return true;
	}

}
