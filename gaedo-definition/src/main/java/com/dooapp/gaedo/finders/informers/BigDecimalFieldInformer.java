package com.dooapp.gaedo.finders.informers;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

public class BigDecimalFieldInformer extends
		ComparableFieldInformer<BigDecimal> implements FieldInformer {

	public BigDecimalFieldInformer(Property source) {
		super(source);
	}

	@Override
	protected BigDecimalFieldInformer clone() {
		return new BigDecimalFieldInformer(source);
	}
}
