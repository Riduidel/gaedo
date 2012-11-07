package com.dooapp.gaedo.finders.collections;

import com.dooapp.gaedo.properties.Property;

public abstract class AbstractBasicContainingEvaluator<DataType> extends
		AbstractBasicEvaluator<DataType> {

	protected final Object contained;

	public AbstractBasicContainingEvaluator(Property field, Object contained) {
		super(field);
		this.contained = contained;
	}

}
