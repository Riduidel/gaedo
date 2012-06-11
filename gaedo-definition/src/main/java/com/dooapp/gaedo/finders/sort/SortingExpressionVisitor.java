package com.dooapp.gaedo.finders.sort;

import java.util.Map.Entry;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.SortingExpression.Direction;
import com.dooapp.gaedo.patterns.Visitor;

public interface SortingExpressionVisitor extends Visitor {

	void startVisit(SortingExpression sortingExpression);

	void endVisit(SortingExpression sortingExpression);
	/**
	 * Visit one sorting entry
	 * @param entry
	 */
	void visit(Entry<FieldInformer, Direction> entry);

}
