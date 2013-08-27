package com.dooapp.gaedo.finders.projection;

import com.dooapp.gaedo.finders.Informer;


/**
 * Interface allowing results of a query on DataType objects to be expressed in terms of an unknown value type
 * @author ndx
 *
 * @param <DataType> the type we're browsing storage for
 * @param <InformerType> informer interface for that type
 * @param <ValueType> the value type we're returning
 */
public interface ProjectionBuilder<ValueType, DataType, InformerType extends Informer<DataType>> {
	public ValueType project(InformerType informer, ValueFetcher fetcher);
}
