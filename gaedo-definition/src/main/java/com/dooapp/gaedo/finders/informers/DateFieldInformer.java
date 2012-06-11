package com.dooapp.gaedo.finders.informers;

import java.lang.reflect.Field;
import java.util.Date;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

public class DateFieldInformer extends ComparableFieldInformer<Date> implements
		FieldInformer {

	public DateFieldInformer(Property source) {
		super(source);
	}
	
	@Override
	protected DateFieldInformer clone() {
		return new DateFieldInformer(source);
	}
}
