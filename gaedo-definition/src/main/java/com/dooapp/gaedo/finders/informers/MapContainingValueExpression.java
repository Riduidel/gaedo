package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.AbstractBasicContainingExpression;
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.properties.Property;

public class MapContainingValueExpression extends
		AbstractBasicContainingExpression implements QueryExpression {

	public MapContainingValueExpression(Property fieldName, Iterable<Property> path, Object contained) {
		super(fieldName, path, contained);
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

}
