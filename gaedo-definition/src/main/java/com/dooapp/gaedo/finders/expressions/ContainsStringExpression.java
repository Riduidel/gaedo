package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class ContainsStringExpression extends AbstractBasicExpression implements QueryExpression {

	private final String contained;

	public String getContained() {
		return contained;
	}

	public ContainsStringExpression(Property source, Iterable<Property> path, String contained) {
		super(source, path);
		this.contained = contained;
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
