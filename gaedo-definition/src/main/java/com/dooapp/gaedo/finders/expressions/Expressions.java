package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;

/**
 * A bunch of static method for easier expression combination
 * 
 * @author ndx
 * 
 */
public class Expressions {
	/**
	 * Combine given expression in a and expression
	 * 
	 * @param expressions
	 * @return a {@link AndQueryExpression} containing all input expressions
	 */
	public static QueryExpression and(QueryExpression... expressions) {
		return new AndQueryExpression(expressions);
	}

	/**
	 * Combine given expression in a or expression
	 * 
	 * @param expressions
	 * @return a {@link OrQueryExpression} containing all input expression
	 */
	public static QueryExpression or(QueryExpression... expressions) {
		return new OrQueryExpression(expressions);
	}

	/**
	 * Combine given expression in a or expression
	 * 
	 * @param expressions
	 * @return a {@link NotQueryExpression} containing input expression
	 */
	public static QueryExpression not(QueryExpression expression) {
		return new NotQueryExpression(expression);
	}

	/**
	 * Create a string representation of the given expression
	 * 
	 * @param expression
	 *            expression to output
	 * @return
	 */
	public static String toString(QueryExpression expression) {
		ToStringVisitor visitor = new ToStringVisitor();
		expression.accept(visitor);
		return visitor.toString();
	}
}
