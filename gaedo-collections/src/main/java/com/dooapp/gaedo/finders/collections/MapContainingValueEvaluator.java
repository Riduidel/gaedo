package com.dooapp.gaedo.finders.collections;

import java.util.Map;

import com.dooapp.gaedo.properties.Property;

public class MapContainingValueEvaluator<DataType> extends
		AbstractBasicContainingEvaluator<DataType> implements
		Evaluator<DataType> {

	public MapContainingValueEvaluator(Property field, Object contained) {
		super(field, contained);
	}


	@Override
	public boolean matches(DataType element) {
		Map<?, ?> value = (Map<?, ?>) getValue(element);
		return value.containsValue(contained);
	}

}
