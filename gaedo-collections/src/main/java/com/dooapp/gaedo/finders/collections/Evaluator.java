package com.dooapp.gaedo.finders.collections;



/**
 * Evaluator is counterpart for evaluating expressions.
 * @author ndx
 *
 */
public interface Evaluator<DataType> {
	/**
	 * Main method used to check if input object matches this evaluator
	 * @param element
	 * @return
	 */
	public boolean matches(DataType element);

	/**
	 * Adds a subevaluator to this evaluator. May fire an {@link UnsupportedOperationException}
	 * @param subEvaluator
	 */
	public void add(Evaluator<DataType> subEvaluator);
}