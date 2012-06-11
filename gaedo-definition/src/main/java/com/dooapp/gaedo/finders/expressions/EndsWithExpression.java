package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class EndsWithExpression extends AbstractBasicExpression implements
		QueryExpression {

	private final String end;

	public String getEnd() {
		return end;
	}

	public EndsWithExpression(Property source, Iterable<Property> path, String end) {
		super(source, path);
		this.end = end;
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
