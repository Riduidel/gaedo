package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Specialized sub-interface of {@link FinderCrudService} for servcies backed by direct access to google datastore
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public interface DatastoreFinderService<DataType, InformerType extends Informer<DataType>> extends
		FinderCrudService<DataType, InformerType>, IdBasedService<DataType> {
	/**
	 * Provides a constant kind for all the objects used by this service
	 * @return
	 */
	String getKind();

	/**
	 * provides the ability to create an object from a source entity
	 * @param entity
	 * @return
	 */
	DataType getObject(Entity entity);

	/**
	 * Provides access to the id management mechanism
	 * @return an implementor of {@link IdManager} interface
	 */
	public IdManager getIdManager();
	
	/**
	 * Get field declared with @Parent annotation for elements of this class
	 * @return a possibly null class
	 */
	public Property getParentField();

	DataType getObjectFromKey(Key key);
}
