package com.dooapp.gaedo.finders.collections;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;

/**
 * Check if source field value is equals to memoized value
 * @author ndx
 *
 */
public class EqualsEvaluator<DataType> extends AbstractBasicEvaluator<DataType> {
	/**
	 * Checked value
	 */
	private final Object value;
	
	public EqualsEvaluator(Property source, Object value) {
		super(source);
		this.value = value;
	}

	public boolean matches(DataType element) {
		Object retrieved = getValue(element);
		return (value==null && retrieved==null) || (value!=null && value.equals(retrieved));
	}
}