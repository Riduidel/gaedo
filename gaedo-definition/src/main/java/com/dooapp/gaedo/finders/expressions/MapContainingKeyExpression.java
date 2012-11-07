package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class MapContainingKeyExpression extends AbstractBasicContainingExpression
		implements QueryExpression {

	public MapContainingKeyExpression(Property source, Iterable<Property> path, Object contained) {
		super(source, path, contained);
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

}
