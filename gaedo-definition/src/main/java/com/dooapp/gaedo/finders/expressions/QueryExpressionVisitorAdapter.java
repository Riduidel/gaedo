package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;

/**
 * An adapter in the sense of the Swing ones : it provides empty implementations to allow extenders not to be too much worried about code quantity
 * @author ndx
 *
 */
public class QueryExpressionVisitorAdapter implements QueryExpressionVisitor {

	@Override
	public void visit(EqualsExpression expression) {
	}

	@Override
	public void startVisit(OrQueryExpression orQueryExpression) {
	}

	@Override
	public void endVisit(OrQueryExpression orQueryExpression) {
	}

	@Override
	public void startVisit(AndQueryExpression andQueryExpression) {
	}

	@Override
	public void endVisit(AndQueryExpression andQueryExpression) {
	}

	@Override
	public void startVisit(NotQueryExpression notQueryExpression) {
	}

	@Override
	public void endVisit(NotQueryExpression notQueryExpression) {
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> lowerThanExpression) {
	}

	@Override
	public void visit(EqualsToIgnoreCaseExpression equalsToIgnoreCaseExpression) {
	}

	@Override
	public void visit(ContainsStringExpression containsStringExpression) {
	}

	@Override
	public void visit(StartsWithExpression startsWithExpression) {
	}

	@Override
	public void visit(EndsWithExpression endsWithExpression) {
	}

	@Override
	public void visit(CollectionContaingExpression collectionContaingExpression) {
	}

	@Override
	public void visit(MapContainingValueExpression mapContainingValueExpression) {
	}

	@Override
	public void visit(MapContainingKeyExpression mapContainingKeyExpression) {
	}

	@Override
	public void visit(AnythingExpression anythingExpression) {
	}

	@Override
	public void visit(MatchesRegexpExpression matchesRegexpExpression) {
	}

}
