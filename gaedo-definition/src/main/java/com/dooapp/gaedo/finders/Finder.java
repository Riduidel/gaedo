package com.dooapp.gaedo.finders;

/**
 * A finder has the ability to return one or more objects of the given type
 * 
 * @author ndx
 * 
 * @param <DataType>
 */
public interface Finder<DataType, InformerType extends Informer<DataType>> {
	/**
	 * Create a query executable statement from a query builder
	 * @param query input query builder
	 * @return output query statement
	 */
	public QueryStatement<DataType, InformerType> matching(QueryBuilder<InformerType> query);
}
