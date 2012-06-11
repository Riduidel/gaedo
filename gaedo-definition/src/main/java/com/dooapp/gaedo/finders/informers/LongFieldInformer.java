package com.dooapp.gaedo.finders.informers;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

public class LongFieldInformer extends ComparableFieldInformer<Long> implements
		FieldInformer {

	public LongFieldInformer(Property source) {
		super(source);
	}

	@Override
	protected LongFieldInformer clone() {
		return new LongFieldInformer(source);
	}
}
