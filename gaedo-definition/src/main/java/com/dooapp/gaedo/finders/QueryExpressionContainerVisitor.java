package com.dooapp.gaedo.finders;

import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.finders.sort.SortingExpressionVisitor;


/**
 * A query statement visitor visits the query statement, and its source query expression
 * @author ndx
 *
 */
public interface QueryExpressionContainerVisitor extends QueryExpressionVisitor, SortingExpressionVisitor {
	/**
	 * Starts the visit of the query statement
	 * @param queryStatement
	 */
	public void startVisit(QueryExpressionContainer queryStatement);
	/**
	 * Ends the visit of the query statement
	 * @param queryStatement
	 */
	public void endVisit(QueryExpressionContainer queryStatement);
}
