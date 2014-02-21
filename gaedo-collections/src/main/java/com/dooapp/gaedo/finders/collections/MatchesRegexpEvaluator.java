package com.dooapp.gaedo.finders.collections;

import java.util.regex.Pattern;

import com.dooapp.gaedo.properties.Property;

public class MatchesRegexpEvaluator<DataType> extends AbstractBasicEvaluator<DataType> implements Evaluator<DataType> {

	private final Pattern pattern;

	public MatchesRegexpEvaluator(Property field, Pattern pattern) {
		super(field);
		this.pattern = pattern;
	}

	@Override
	public boolean matches(DataType element) {
		String value = (String) getValue(element);
		return pattern.matcher(value).matches();
	}

}
