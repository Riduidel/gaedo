package com.dooapp.gaedo.finders.expressions;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import com.dooapp.gaedo.finders.QueryExpression;

public abstract class AggregatingQueryExpression implements QueryExpression {
	protected Collection<QueryExpression> expressions = new LinkedList<QueryExpression>();
	
	public AggregatingQueryExpression(QueryExpression... expressions) {
		super();
		this.expressions.addAll(Arrays.asList(expressions));
	}

	public AggregatingQueryExpression(Collection<QueryExpression> expressions) {
		this();
		add(expressions.toArray(new QueryExpression[expressions.size()]));
	}
	
	/**
	 * Add new and conditions
	 * @param toAdd
	 */
	public void add(QueryExpression...toAdd) {
		this.expressions.addAll(Arrays.asList(toAdd)); 
	}

	/**
	 * A common way to avoid code duplication : put it in common superclass.
	 * However, to ensure type is set correctly, the various startVisit method are delegated to abstract one
	 * @see #startVisitFor(QueryExpressionVisitor)
	 * @see #endVisitFor(QueryExpressionVisitor)
	 */
	public void accept(QueryExpressionVisitor visitor) {
		startVisitFor(visitor);
		for (QueryExpression exp : expressions) {
			exp.accept(visitor);
		}
		endVisitFor(visitor);
	}

	protected abstract void endVisitFor(QueryExpressionVisitor visitor);

	protected abstract void startVisitFor(QueryExpressionVisitor visitor);
}
