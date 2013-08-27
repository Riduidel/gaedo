package com.dooapp.gaedo.finders.informers;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.GreaterThanExpression;
import com.dooapp.gaedo.finders.expressions.LowerThanExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Base class for all comparable objects fields informers. It provides
 * comparison methods for all kinds of comparisons.
 *
 * @author ndx
 *
 * @param <ComparableType>
 */
public abstract class ComparableFieldInformer<ComparableType extends Comparable<ComparableType>>
		extends ObjectFieldInformer<ComparableType> implements FieldInformer<ComparableType> {

	public ComparableFieldInformer(Property source) {
		super(source);
	}

	/**
	 * Check that number is lower than given value
	 *
	 * @param other
	 *            value to compare to
	 * @return a {@link LowerThanExpression}
	 */
	public QueryExpression lowerThan(ComparableType other) {
		return new LowerThanExpression<ComparableType>(source, getFieldPath(), other, true);
	}

	/**
	 * Check that number is greater than value
	 *
	 * @param other
	 *            value compared to
	 * @return a {@link GreaterThanExpression}
	 */
	public QueryExpression greaterThan(ComparableType other) {
		return new GreaterThanExpression<ComparableType>(source, getFieldPath(), other, true);
	}
}
