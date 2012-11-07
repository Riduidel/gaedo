package com.dooapp.gaedo.finders.informers;

import java.lang.reflect.Field;
import java.math.BigInteger;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

public class BigIntegerFieldInformer extends
		ComparableFieldInformer<BigInteger> implements FieldInformer {

	public BigIntegerFieldInformer(Property source) {
		super(source);
	}
	
	@Override
	protected BigIntegerFieldInformer clone() {
		return new BigIntegerFieldInformer(source);
	}
}
