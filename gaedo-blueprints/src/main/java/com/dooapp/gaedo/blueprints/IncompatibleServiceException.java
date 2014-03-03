package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.finders.FinderCrudService;

/**
 * Given service is incompatible with general contract of {@link IndexableGraphBackedFinderService}
 * @author ndx
 *
 */
public class IncompatibleServiceException extends BluePrintsCrudServiceException {

	public IncompatibleServiceException(FinderCrudService service, Class<? extends Object> valueClass) {
		super("service "+service.getClass().getName()+" is associated with values of type "+valueClass.getCanonicalName()+" but IS NOT compatible with requirements of "+IndexableGraphBackedFinderService.class.getName());
	}
}
