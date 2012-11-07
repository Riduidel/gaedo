package com.dooapp.gaedo.finders.collections;

import java.util.Collection;

import com.dooapp.gaedo.properties.Property;

public class CollectionContainingEvaluator<DataType> extends AbstractBasicContainingEvaluator<DataType> implements Evaluator<DataType> {

	public CollectionContainingEvaluator(Property field, Object contained) {
		super(field, contained);
	}

	@Override
	public boolean matches(DataType element) {
		Collection<?> value = (Collection<?>) getValue(element);
		return value.contains(contained);
	}

}
