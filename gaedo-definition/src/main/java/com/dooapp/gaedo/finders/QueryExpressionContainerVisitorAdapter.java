package com.dooapp.gaedo.finders;

import java.util.Map.Entry;

import com.dooapp.gaedo.finders.SortingExpression.Direction;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.AnythingExpression;
import com.dooapp.gaedo.finders.expressions.CollectionContaingExpression;
import com.dooapp.gaedo.finders.expressions.ContainsStringExpression;
import com.dooapp.gaedo.finders.expressions.EndsWithExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.finders.expressions.GreaterThanExpression;
import com.dooapp.gaedo.finders.expressions.LowerThanExpression;
import com.dooapp.gaedo.finders.expressions.MapContainingKeyExpression;
import com.dooapp.gaedo.finders.expressions.NotQueryExpression;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitorAdapter;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;

public class QueryExpressionContainerVisitorAdapter extends QueryExpressionVisitorAdapter implements QueryExpressionContainerVisitor {

	@Override
	public void startVisit(SortingExpression sortingExpression) {
	}

	@Override
	public void endVisit(SortingExpression sortingExpression) {
	}

	@Override
	public void visit(Entry<FieldInformer, Direction> entry) {
	}

	@Override
	public void startVisit(QueryExpressionContainer queryStatement) {
	}

	@Override
	public void endVisit(QueryExpressionContainer queryStatement) {
	}
}
