package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class EqualsExpression extends AbstractBasicExpression implements
		QueryExpression {

	/**
	 * Compared object
	 */
	private final Object value;

	public Object getValue() {
		return value;
	}

	/**
	 * Ensure field is equals to input expression
	 * @param name
	 * @param value
	 * @param value2 
	 */
	public EqualsExpression(Property name, Iterable<Property> path, Object value) {
		super(name, path);
		this.value = value;
	}

	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
