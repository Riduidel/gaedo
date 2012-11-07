package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Expression for a collection containing an expected object
 * @author ndx
 *
 */
public class CollectionContaingExpression extends AbstractBasicContainingExpression
		implements QueryExpression {

	public CollectionContaingExpression(Property fieldName, Iterable<Property> path, Object contained) {
		super(fieldName, path, contained);
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
