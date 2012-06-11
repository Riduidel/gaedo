package com.dooapp.gaedo.finders.collections;

import java.util.LinkedList;
import java.util.List;

/**
 * or combinative evaluator, combines the result of all contained evaluators with a or clause
 * @author ndx
 *
 */
class OrEvaluator<DataType> implements Evaluator<DataType> {
	private List<Evaluator<DataType>> inner = new LinkedList<Evaluator<DataType>>();

	public void add(Evaluator<DataType> e) {
		inner.add(e);
	}

	public boolean matches(DataType element) {
		boolean result = false;
		for(Evaluator<DataType> e : inner) {
			result|=e.matches(element);
		}
		return result;
	}
}