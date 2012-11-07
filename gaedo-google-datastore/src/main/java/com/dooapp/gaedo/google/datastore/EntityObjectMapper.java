/**
 * 
 */
package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.Entity;

public interface EntityObjectMapper<DataType> {
	public void map(Entity entity, DataType object, String entityName, Property value);
}