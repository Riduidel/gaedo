package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Field informer locator is able to combine all existing informations about a field in order to build the most comprehensive {@link FieldInformer}
 * @author Nicolas
 *
 */
public interface FieldInformerLocator {

	/**
	 * Given an input {@link Property}, a {@link FieldInformer} will use its own strategy to provide the most meaningfull FieldInformer
	 * @param field
	 * @return an informer for the field, or null if none can be found
	 */
	FieldInformer getInformerFor(Property field);

	/**
	 * Fallback method allowing one to check if a field exist for a given class, even if not found using {@link #getInformerFor(Property)}
	 * @param informedClass class in which this field is looked
	 * @param fieldName searched field name
	 * @return an informer for the field, or null if none can be found
	 */
	FieldInformer getInformerFor(Class informedClass, String fieldName);

}
