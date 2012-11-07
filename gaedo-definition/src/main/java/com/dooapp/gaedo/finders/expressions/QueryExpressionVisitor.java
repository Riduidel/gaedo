package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;
import com.dooapp.gaedo.patterns.Visitor;

/**
 * Interface implemented by visitors constructing query statements from query
 * expressions
 * 
 * @author ndx
 * 
 */
public interface QueryExpressionVisitor extends Visitor {
	/**
	 * Transforms the equals expression in usable code
	 * 
	 * @param expression
	 */
	void visit(EqualsExpression expression);

	/**
	 * Transforms the or expression in usable code
	 * 
	 * @param orQueryExpression
	 */
	void startVisit(OrQueryExpression orQueryExpression);

	/**
	 * Terminates processing of an or expression
	 * 
	 * @param orQueryExpression
	 */
	void endVisit(OrQueryExpression orQueryExpression);

	/**
	 * Transforms the and expression in usable code
	 * 
	 * @param andQueryExpression
	 */
	void startVisit(AndQueryExpression andQueryExpression);

	/**
	 * Terminates processing of an and expression
	 * 
	 * @param andQueryExpression
	 */
	void endVisit(AndQueryExpression andQueryExpression);

	/**
	 * Transforms the not expression in usable code
	 * 
	 * @param notQueryExpression
	 */
	void startVisit(NotQueryExpression notQueryExpression);

	/**
	 * Terminates the visit of the not expression
	 * 
	 * @param notQueryExpression
	 */
	void endVisit(NotQueryExpression notQueryExpression);

	/**
	 * Transforms the greater than expression in usable code
	 * 
	 * @param greaterThanExpression
	 */
	<ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression);

	/**
	 * Transforms the lower than expression in usable code
	 * 
	 * @param lowerThanExpression
	 */
	<ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> lowerThanExpression);

	/**
	 * Transforms the containsString expression in usable code
	 * @param containsStringExpression
	 */
	void visit(ContainsStringExpression containsStringExpression);

	/**
	 * Transforms the startsWith expression in usable code
	 * @param containsStringExpression
	 */
	void visit(StartsWithExpression startsWithExpression);

	/**
	 * Transforms the endsWith expression in usable code
	 * @param endsWithExpression
	 */
	void visit(EndsWithExpression endsWithExpression);

	/**
	 * Transforms the collection containing expression in usable code
	 * @param collectionContaingExpression
	 */
	void visit(CollectionContaingExpression collectionContaingExpression);

	/**
	 * Transforms the map containing key expression in usable code
	 * @param mapContainingValueExpression
	 */
	void visit(MapContainingValueExpression mapContainingValueExpression);

	/**
	 * Transforms the map containing value expression in usable code
	 * @param mapContainingKeyExpression
	 */
	void visit(MapContainingKeyExpression mapContainingKeyExpression);

	/**
	 * Visits an anything expression
	 * @param anythingExpression
	 */
	void visit(AnythingExpression anythingExpression);

}
