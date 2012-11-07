package com.dooapp.gaedo.extensions.views;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;

/**
 * A service implementing this interface can be focused on a view, whatever it may means.
 * As a typical (ie the one that required that interface creation), a graph based finder service may be focused on a named graph list, in which case created relationships
 * will all be defined in the named graphs on which the service currently focuses.
 * @author ndx
 *
 * @param <DataType> data type managed by this service
 * @param <InformerType> informer type associated to data
 */
public interface InViewService<DataType, InformerType extends Informer<DataType>, FocusType> extends FinderCrudService<DataType, InformerType> {
	/**
	 * Focus on the given focus object 
	 * @param lens
	 * @return a DIFFERENT service focused on that lens. The fact that the new service is a different one is a pure legacy one : as 
	 * existing services are not at all modeled for that (and especially the dynamic part) it is not possible to alter service (beside the fact it could easily reveal to be a crazy bad idea).
	 */
	public InViewService<DataType, InformerType, FocusType> focusOn(FocusType lens);
	
	/**
	 * Get this service associated lens
	 * @return lens used by this service to view/access/update data
	 */
	public FocusType getLens();
}
