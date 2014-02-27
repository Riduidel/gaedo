package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class InstanceOfExpression extends AbstractBasicExpression implements QueryExpression {

	private final Class<?> type;

	public InstanceOfExpression(Property source, Iterable<Property> fieldPath, Class<?> type) {
		super(source, fieldPath);
		this.type = type;
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the type
	 * @category getter
	 * @category type
	 */
	public Class<?> getType() {
		return type;
	}

}
