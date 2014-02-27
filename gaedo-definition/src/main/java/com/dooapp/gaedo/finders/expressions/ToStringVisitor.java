package com.dooapp.gaedo.finders.expressions;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;
import com.dooapp.gaedo.properties.Property;

public class ToStringVisitor implements QueryExpressionVisitor {
	private static final String NULL = "***null***";
	private String indent = "\t";
	private int deepness = 0;
	private StringBuilder out = new StringBuilder();

	private StringBuilder deepnessString() {
		StringBuilder sOut = new StringBuilder();
		for (int i = 0; i < deepness; i++) {
			sOut.append(indent);
		}
		return sOut;
	}

	public void endVisit(OrQueryExpression orQueryExpression) {
		deepness--;
	}

	public void endVisit(AndQueryExpression andQueryExpression) {
		deepness--;
	}

	public void endVisit(NotQueryExpression notQueryExpression) {
		deepness--;
	}

	public void startVisit(OrQueryExpression orQueryExpression) {
		out.append(deepnessString()).append("OR").append("\n");
		deepness++;
	}

	public void startVisit(AndQueryExpression andQueryExpression) {
		out.append(deepnessString()).append("AND").append("\n");
		deepness++;
	}

	public void startVisit(NotQueryExpression notQueryExpression) {
		out.append(deepnessString()).append("NOT").append("\n");
		deepness++;
	}

	@Override
	public void visit(AnythingExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append("\tis anything (yep, really anything)")
				.append("\n");
	}

	public void visit(EqualsExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append("\t==?\t")
				.append(unNull(expression.getValue()).toString()).append("\n");
	}

	/**
	 * Get text associated to field part of basic expression
	 * @param expression input expression
	 * @return a view of the field
	 */
	private String getFieldText(AbstractBasicExpression expression) {
		Property f = expression.getField();
		if(f==null)
			return "this";
		else
			return f.toGenericString();
	}

	@Override
	public String toString() {
		return out.toString();
	}

	/**
	 * Replace a potentially null value with a non null one
	 * @param value
	 * @return the value {@value #NULL}
	 */
	private Object unNull(Object value) {
		return value==null ? NULL : value;
	}

	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
		out.append(deepnessString()).append(
				getFieldText(greaterThanExpression)).append(
				">").append(greaterThanExpression.isStrictly() ? "" : "=")
				.append("?")
				.append(unNull(greaterThanExpression.getValue()).toString()).append(
						"\n");
	}

	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> lowerThanExpression) {
		out.append(deepnessString()).append(
				getFieldText(lowerThanExpression)).append(
				"<").append(lowerThanExpression.isStrictly() ? "" : "=")
				.append("?").append(unNull(lowerThanExpression.getValue()).toString())
				.append("\n");
	}

	@Override
	public void visit(EqualsToIgnoreCaseExpression expression) {
		out.append(deepnessString()).append(
						getFieldText(expression)).append(
						" ==(a==A)? ").append(expression.getCompared()).append("\n");
	}

	@Override
	public void visit(ContainsStringExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append(
				" contains? ").append(expression.getContained()).append("\n");
	}

	@Override
	public void visit(StartsWithExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append(
				" startsWith? ").append(expression.getStart()).append("\n");
	}

	@Override
	public void visit(EndsWithExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append(
				" endsWith? ").append(expression.getEnd()).append("\n");
	}

	@Override
	public void visit(CollectionContaingExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append(
				" contains? ").append(unNull(expression.getContained()).toString()).append("\n");
	}

	@Override
	public void visit(MapContainingValueExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append(
				" containsKey? ").append(unNull(expression.getContained()).toString()).append("\n");
	}

	@Override
	public void visit(MapContainingKeyExpression expression) {
		out.append(deepnessString()).append(
				getFieldText(expression)).append(
				" containsValue? ").append(unNull(expression.getContained()).toString()).append("\n");
	}

	@Override
	public void visit(MatchesRegexpExpression expression) {
		out.append(deepnessString()).append(
						getFieldText(expression)).append(
						" matches? ").append(expression.getPattern().toString()).append("\n");
	}

	@Override
	public void visit(InstanceOfExpression expression) {
		out.append(deepnessString()).append(
						getFieldText(expression)).append(
						" instanceof? ").append(expression.getType().getCanonicalName()).append("\n");
	}
}
