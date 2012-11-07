package com.dooapp.gaedo.finders.dynamic;

import java.util.Collection;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;

/**
 * Types of combinations.
 * Should correspond to a subset of {@link Expressions} declared ones
 * @author ndx
 *
 */
enum Combinator {
	And("And"),
	Or("Or");
	
	private final String text;

	private Combinator(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public QueryExpression create(Collection<QueryExpression> resolvedExpressions) {
		switch(this) {
		case And:
			return new AndQueryExpression(resolvedExpressions);
		case Or:
			return new OrQueryExpression(resolvedExpressions);
		}
		throw new UnsupportedOperationException("used Combinator is not one of declared ones ???");
	}
}