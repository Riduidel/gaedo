package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;

/**
 * various utilities (constants, casters, ...)
 * 
 * @author ndx
 * 
 */
public class Utils {

	/**
	 * Method added to solve a storage issue : when storing a priitive type, it
	 * may not be returned in its original type (typically, float are stored as
	 * doubles) this method provides conversion code.
	 * 
	 * @param type
	 * @param property
	 * @return
	 */
	public static Object getCastedProperty(Class<?> type, Object property) {
		if (type.isPrimitive()) {
			if (Integer.TYPE.equals(type)) {
				return ((Number) property).intValue();
			} else if (Long.TYPE.equals(type)) {
				return ((Number) property).longValue();
			} else if (Float.TYPE.equals(type)) {
				return ((Number) property).floatValue();
			} else if (Double.TYPE.equals(type)) {
				return ((Number) property).doubleValue();
			} else if (Byte.TYPE.equals(type)) {
				return ((Number) property).byteValue();
			} else if (Short.TYPE.equals(type)) {
				return ((Number) property).shortValue();
			} else if (Boolean.TYPE.equals(type)) {
				return property;
			}
		}
		return type.cast(property);
	}

	public static final String SIZE = ".size";

	/**
	 * Build a datastore field name from a field and associated class
	 * 
	 * @param f
	 * @return
	 */
	public static String getDatastoreFieldName(Property f) {
		return f.getDeclaringClass().getSimpleName() + "." + f.getName();
	}

	/**
	 * Property used to locate a value field in a collection data
	 */
	public static final String COLLECTION_VALUE_PROPERTY = "value";
	/**
	 * Property used to locate a key field in a map data
	 */
	public static final String MAP_KEY_PROPERTY = "key";
	/**
	 * Property used to locate a value field in a map data
	 */
	public static final String MAP_VALUE_PROPERTY = "value";

}
