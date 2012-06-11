package com.dooapp.gaedo.finders.expressions;

import java.util.Collection;

import com.dooapp.gaedo.finders.QueryExpression;

public class OrQueryExpression extends AggregatingQueryExpression implements QueryExpression {

	public OrQueryExpression(Collection<QueryExpression> expressions) {
		super(expressions);
	}

	public OrQueryExpression(QueryExpression... expressions) {
		super(expressions);
	}

	/**
	 * Uses {@link Expressions#toString(QueryExpression))} to output a string
	 * view
	 */
	@Override
	public String toString() {
		return Expressions.toString(this);
	}

	@Override
	protected void endVisitFor(QueryExpressionVisitor visitor) {
		visitor.endVisit(this);
	}

	@Override
	protected void startVisitFor(QueryExpressionVisitor visitor) {
		visitor.startVisit(this);
	}
}
