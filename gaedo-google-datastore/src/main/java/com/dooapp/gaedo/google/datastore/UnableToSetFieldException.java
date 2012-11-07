/**
 * 
 */
package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.properties.Property;

/**
 * For various reasons, it was impossible to set value of given field
 * @author Nicolas
 *
 */
public class UnableToSetFieldException extends GAECrudServiceException {
	public UnableToSetFieldException(Exception e, Property f) {
		super("Unable to set value of field "+f.toGenericString(), e);
	}
}