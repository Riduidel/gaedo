package com.dooapp.gaedo.finders.projection;

import com.dooapp.gaedo.finders.FieldInformer;

/**
 * ValueFetcher is able, given a FieldInformer, to get the value for that informer and for a given query. Black magic ? Sure not !
 * It is in fact an internal component which is accessed by the query
 * @author ndx
 *
 */
public interface ValueFetcher {

	/**
	 * Get the value for currently evaluated path of the given property descriptor
	 * @param propertyDescriptor
	 * @return value of that property in current context
	 */
	<Type> Type getValue(FieldInformer<Type> propertyDescriptor);

}
