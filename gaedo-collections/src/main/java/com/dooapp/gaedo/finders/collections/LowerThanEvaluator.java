package com.dooapp.gaedo.finders.collections;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;

/**
 * This evaluator uses non-typesafe Comparable interfaces, since the type has already been ensured elsewhere.
 * @author ndx
 *
 * @param <DataType>
 */
@SuppressWarnings("unchecked")
public class LowerThanEvaluator<DataType> extends
		AbstractBasicEvaluator<DataType> implements Evaluator<DataType> {

	private boolean strictly;
	private Comparable value;

	public LowerThanEvaluator(Property fieldName, boolean strictly, Comparable value) {
		super(fieldName);
		this.strictly = strictly;
		this.value = value;
	}

	public boolean matches(DataType element) {
		Comparable fieldValue = (Comparable<?>) getValue(element);
		return strictly ? fieldValue.compareTo(value)<0 : fieldValue.compareTo(value)<=0;
	}

}
