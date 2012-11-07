/**
 * 
 */
package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.properties.Property;

/**
 * For various reasons, we were unable to get value of given field
 * @author Nicolas
 *
 */
public class UnableToGetFieldException extends GAECrudServiceException {
	public UnableToGetFieldException(Exception e, Property f) {
		super("Unable to get value of field "+f.toGenericString(), e);
	}
}