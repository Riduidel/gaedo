package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class StartsWithExpression extends AbstractBasicExpression implements
		QueryExpression {

	private final String start;

	public String getStart() {
		return start;
	}

	public StartsWithExpression(Property source, Iterable<Property> path, String start) {
		super(source, path);
		this.start = start;
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

}
