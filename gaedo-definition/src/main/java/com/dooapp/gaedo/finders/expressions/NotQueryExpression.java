package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;

public class NotQueryExpression implements QueryExpression {

	private QueryExpression expression;

	public NotQueryExpression(QueryExpression expression) {
		this.expression = expression;
	}

	public void accept(QueryExpressionVisitor visitor) {
		visitor.startVisit(this);
		expression.accept(visitor);
		visitor.endVisit(this);
	}

	/**
	 * Uses {@link Expressions#toString(QueryExpression))} to output a string
	 * view
	 */
	@Override
	public String toString() {
		return Expressions.toString(this);
	}
}
