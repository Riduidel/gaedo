package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class DoubleFieldInformer extends ComparableFieldInformer<Double>
		implements FieldInformer {

	public DoubleFieldInformer(Property source) {
		super(source);
	}
	
	@Override
	protected DoubleFieldInformer clone() {
		return new DoubleFieldInformer(source);
	}
}
