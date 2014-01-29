package com.dooapp.gaedo.blueprints.transformers;

/**
 * Some utility methods allowing one to store class-full data into class-less fields (Object and Serializable, as an example)
 * @author ndx
 *
 */
public class ClassIdentifierHelper {

	public static final char CLASS_VALUE_SEPARATOR = ':';

	/**
	 * Get type prefix of the given literal transformer for later loading it
	 * @param propertyValue a value in which we want to extract type prefix
	 * @return the value before ":"
	 */
	public static String getTypePrefix(String propertyValue) {
		return propertyValue.substring(0, propertyValue.indexOf(CLASS_VALUE_SEPARATOR));
	}

	public static String getValueIn(String propertyValue) {
		return propertyValue.substring(propertyValue.indexOf(CLASS_VALUE_SEPARATOR)+1);
	}

	public static String toString(Class<?> valueClass, String string) {
		return valueClass.getCanonicalName()+CLASS_VALUE_SEPARATOR+string;
	}
}
