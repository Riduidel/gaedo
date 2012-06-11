package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class BooleanFieldInformer extends ObjectFieldInformer implements
		FieldInformer {

	public BooleanFieldInformer(Property source) {
		super(source);
	}

	public QueryExpression isTrue() {
		return equalsTo(Boolean.TRUE);
	}

	public QueryExpression isFalse() {
		return equalsTo(Boolean.FALSE);
	}
	
	@Override
	protected BooleanFieldInformer clone() {
		return new BooleanFieldInformer(source);
	}
}
