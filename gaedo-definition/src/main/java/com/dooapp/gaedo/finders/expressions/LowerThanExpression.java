package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Compare that this field value is lower than the given comparable value
 * 
 * @author ndx
 * 
 */
public class LowerThanExpression<ComparableType extends Comparable<ComparableType>>
		extends AbstractBasicExpression implements QueryExpression {

	/**
	 * Set to true for strict comparison, false for lenient one
	 */
	private final boolean strictly;
	/**
	 * Value compared to
	 */
	private final ComparableType value;

	public LowerThanExpression(Property fieldName, Iterable<Property> path, ComparableType other,
			boolean strictly) {
		super(fieldName, path);
		this.value = other;
		this.strictly = strictly;
	}

	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public ComparableType getValue() {
		return value;
	}

	public boolean isStrictly() {
		return strictly;
	}

}
