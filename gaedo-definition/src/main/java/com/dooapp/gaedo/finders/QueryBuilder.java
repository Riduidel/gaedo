package com.dooapp.gaedo.finders;

/**
 * Builds a query for the given object type
 * @author Nicolas
 *
 * @param <InformerType> type of informer usable by this query builder
 */
public interface QueryBuilder<InformerType extends Informer<?>> {
	/**
	 * Create a query expression from the input informer
	 * @param informer informer used to build the query
	 * @return a query expression allowing one to find the required objects
	 */
	public QueryExpression createMatchingExpression(InformerType informer);
}
