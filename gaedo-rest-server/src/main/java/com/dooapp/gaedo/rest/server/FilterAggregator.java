package com.dooapp.gaedo.rest.server;

import java.util.Map;

import com.dooapp.gaedo.finders.expressions.AggregatingQueryExpression;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;

public enum FilterAggregator {
	AND("__and__"),
	OR("__or__");
	
	public static FilterAggregator get(Map<String, Object> value) {
		if(value.containsKey(AND.key))
			return AND;
		else if(value.containsKey(OR.key))
			return OR;
		return null;
	}

	/**
	 * An aggregator map is a one-value map containing only {@link #AND} or {@link #OR}
	 * @param value map to check
	 * @return true if possible, false elsewhere
	 */
	public static boolean isAggregationMap(Map<String, Object> value) {
		return value.size()==1 && (value.containsKey(AND.key) || value.containsKey(OR.key));
	}

	private final String key;

	private FilterAggregator(String key) {
		this.key = key;
	}

	public AggregatingQueryExpression createExpression() {
		switch(this) {
		case AND:
			return new AndQueryExpression();
		case OR:
			return new OrQueryExpression();
		default:
			return null;
		}
	}

	public String getKey() {
		return key;
	}
}
