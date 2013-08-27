package com.dooapp.gaedo.finders.collections;

import java.util.Stack;

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
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;

/**
 * Matcher will be used to check if input object matches local constraints
 * @author ndx
 *
 * @param <DataType>
 */
public class Matcher<DataType> implements QueryExpressionVisitor {
	/**
	 * A stack is maintained during visiting elements, but ony its 0 element is interesting
	 */
	private Stack<Evaluator<DataType>> evaluators = new Stack<Evaluator<DataType>>();

	public Matcher() {
		evaluators.push(new AndEvaluator<DataType>());
	}

	public void endVisit(AndQueryExpression andQueryExpression) {
		evaluators.pop();
	}

	public void endVisit(NotQueryExpression notQueryExpression) {
		evaluators.pop();
	}

	public void endVisit(OrQueryExpression orQueryExpression) {
		evaluators.pop();
	}

	/**
	 * Check if this matcher matches the given object. For that, all inner matchers are used. The root matcher will be used to start evaluating.
	 * Hopefully, this root matcher is a And one
	 * @param element
	 */
	public boolean matches(DataType element) {
		return evaluators.get(0).matches(element);
	}

	public void startVisit(AndQueryExpression andQueryExpression) {
		add(new AndEvaluator<DataType>());
	}

	/**
	 * Adds an evaluator on the top of the stack
	 * @param evaluator
	 */
	private void add(Evaluator<DataType> evaluator) {
		evaluators.peek().add(evaluator);
		evaluators.push(evaluator);
	}

	public void startVisit(NotQueryExpression notQueryExpression) {
		add(new NotEvaluator<DataType>());
	}

	public void startVisit(OrQueryExpression orQueryExpression) {
		add(new OrEvaluator<DataType>());
	}

	@Override
	public void visit(AnythingExpression expression) {
		evaluators.peek().add(new AnythingEvaluator<DataType>(expression.getField()));
	}

	public void visit(EqualsExpression expression) {
		evaluators.peek().add(new EqualsEvaluator<DataType>(expression.getField(), expression.getValue()));
	}

	@Override
	public String toString() {
		return evaluators.get(0).toString();
	}

	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> expression) {
		evaluators.peek().add(new GreaterThanEvaluator<DataType>(expression.getField(), expression.isStrictly(), expression.getValue()));

	}

	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> expression) {
		evaluators.peek().add(new LowerThanEvaluator<DataType>(expression.getField(), expression.isStrictly(), expression.getValue()));
	}

	@Override
	public void visit(ContainsStringExpression expression) {
		evaluators.peek().add(new ContainsStringEvaluator<DataType>(expression.getField(), expression.getContained()));
	}

	@Override
	public void visit(StartsWithExpression expression) {
		evaluators.peek().add(new StartsWithEvaluator<DataType>(expression.getField(), expression.getStart()));
	}

	@Override
	public void visit(EndsWithExpression expression) {
		evaluators.peek().add(new EndsWithEvaluator<DataType>(expression.getField(), expression.getEnd()));
	}

	@Override
	public void visit(CollectionContaingExpression expression) {
		evaluators.peek().add(new CollectionContainingEvaluator<DataType>(expression.getField(), expression.getContained()));

	}

	@Override
	public void visit(MapContainingValueExpression expression) {
		evaluators.peek().add(new MapContainingKeyEvaluator<DataType>(expression.getField(), expression.getContained()));
	}

	@Override
	public void visit(MapContainingKeyExpression expression) {
		evaluators.peek().add(new MapContainingValueEvaluator<DataType>(expression.getField(), expression.getContained()));
	}
}