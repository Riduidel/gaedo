package com.dooapp.gaedo.finders.expressions;

import java.util.Arrays;
import java.util.Collection;

import com.dooapp.gaedo.finders.QueryExpression;

/**
 * Expression indicating contained expression should all evaluate to true
 * @author ndx
 *
 */
public class AndQueryExpression extends AggregatingQueryExpression implements QueryExpression {

	public AndQueryExpression(Collection<QueryExpression> expressions) {
		super(expressions);
	}

	public AndQueryExpression(QueryExpression... expressions) {
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
