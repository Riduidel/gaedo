package com.dooapp.gaedo.finders.collections;

import java.util.LinkedList;
import java.util.List;

/**
 * Evaluator used to combine with a and a list of others evaluators
 * @author ndx
 *
 */
class AndEvaluator<DataType> implements Evaluator<DataType> {
	public List<Evaluator<DataType>> inner = new LinkedList<Evaluator<DataType>>();

	public void add(Evaluator<DataType> e) {
		inner.add(e);
	}

	public boolean matches(DataType element) {
		boolean result = true;
		for(Evaluator<DataType> e : inner) {
			result&=e.matches(element);
		}
		return result;
	}
}