package com.dooapp.gaedo.finders;

/**
 * A sorting builder allow the creation of a sorting expression from an informer
 * @author ndx
 *
 */
public interface SortingBuilder<InformerType extends Informer<?>> {
	/**
	 * Creates a sorting expression from an informer. usually, this methods involves a call to SortingExpressionImpl constructor followed by calls to {@link SortingExpression#add(FieldInformer, com.dooapp.gaedo.finders.SortingExpression.Direction)}
	 * @param informer source class informer
	 * @return a sorting expression allowing sorting of data
	 */
	public SortingExpression createSortingExpression(InformerType informer);
}
