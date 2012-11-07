package com.dooapp.gaedo.finders.id;

import java.util.Collection;

import com.dooapp.gaedo.AbstractCrudService;
import com.dooapp.gaedo.properties.Property;

/**
 * An id based service is a service for which at least one id field is known
 * These id fields can be used to perform find operations
 * @author ndx
 *
 */
public interface IdBasedService<DataType> extends AbstractCrudService<DataType> {
	/**
	 * Find an object based on its id. If id do not match this service data, an exception may be thrown.
	 * @param id
	 * @return
	 */
	public DataType findById(Object...id);
	
	/**
	 * Get the property associated with id field
	 * @return
	 */
	public Collection<Property> getIdProperties();
	
	/**
	 * Try to assign given id to given object
	 * @param value object to assign id to
	 * @param id collection of id to assign to
	 * @return true if id was successfully assignated, false otherwise
	 */
	public boolean assignId(DataType value, Object...id);
}
