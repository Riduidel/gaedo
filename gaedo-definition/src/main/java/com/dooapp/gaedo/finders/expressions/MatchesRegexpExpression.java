package com.dooapp.gaedo.finders.expressions;

import java.util.regex.Pattern;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public class MatchesRegexpExpression extends AbstractBasicExpression implements QueryExpression {

	private final Pattern pattern;

	public MatchesRegexpExpression(Property source, Iterable<Property> fieldPath, Pattern pattern) {
		super(source, fieldPath);
		this.pattern = pattern;
	}

	@Override
	public void accept(QueryExpressionVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the pattern
	 * @category getter
	 * @category pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

}
