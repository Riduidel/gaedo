package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Compare field content with given value
 * 
 * @author ndx
 * 
 */
public class GreaterThanExpression<ComparableType extends Comparable<ComparableType>>
		extends AbstractBasicExpression implements QueryExpression {

	/**
	 * Value compared to
	 */
	private final ComparableType value;

	public ComparableType getValue() {
		return value;
	}

	public boolean isStrictly() {
		return strictly;
	}

	/**
	 * Strict comparison or not
	 */
	private final boolean strictly;

	public GreaterThanExpression(Property fieldName, Iterable<Property> path, ComparableType value,
			boolean strictly) {
		super(fieldName, path);
		this.value = value;
		this.strictly = strictly;
	}

	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

}
