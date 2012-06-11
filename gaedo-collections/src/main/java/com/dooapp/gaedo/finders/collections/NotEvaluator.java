package com.dooapp.gaedo.finders.collections;

/**
 * Reverse evaluator. This evaluator simply inverst result of contained evaluator
 * @author ndx
 *
 */
class NotEvaluator<DataType> implements Evaluator<DataType> {
	/**
	 * Inner evaluator, which result will be reverted
	 */
	public Evaluator<DataType> inner;

	public void add(Evaluator<DataType> e) {
		inner=e;
	}

	public boolean matches(DataType element) {
		return !inner.matches(element);
	}
}