package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Check if field has any value (in other words, the field is simply present)
 * @author ndx
 *
 */
public class AnythingExpression extends AbstractBasicExpression implements
		QueryExpression {

	public AnythingExpression(Property source, Iterable<Property> fieldPath) {
		super(source, fieldPath);
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

}
