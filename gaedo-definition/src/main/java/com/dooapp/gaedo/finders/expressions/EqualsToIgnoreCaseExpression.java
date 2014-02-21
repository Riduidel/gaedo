package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class EqualsToIgnoreCaseExpression extends AbstractBasicExpression implements QueryExpression {

	private final String compared;

	public EqualsToIgnoreCaseExpression(Property source, Iterable<Property> fieldPath, String comparedTo) {
		super(source, fieldPath);
		this.compared = comparedTo;
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the compared
	 * @category getter
	 * @category compared
	 */
	public String getCompared() {
		return compared;
	}

}
