package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.properties.Property;

/**
 * Some utility methods allowing one to store class-full data into class-less fields (Object and Serializable, as an example)
 * @author ndx
 *
 */
public class LiteralHelper {

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

	/**
	 * Get the property text for the given literal value, considered as a value of given property type
	 * @param propertyClass supposed class of property. If it is not a literal (Object or Serializable, as an example),
	 * then type is infered dynamically and set as prefix
	 * @param propertyValue
	 * @return
	 */
	public static String getLiteralTextFor(Class propertyClass, Object propertyValue) {
		String text = null;
		if(Literals.containsKey(propertyClass)) {
			LiteralTransformer<Object> transformer = Literals.get(propertyClass);
			text = transformer.toString(propertyValue);
		} else {
			Class<?> valueClass = propertyValue.getClass();
			LiteralTransformer<Object> transformer = Literals.get(valueClass);
			text = toString(valueClass, transformer.toString(propertyValue));
		}
		return text;
	}

	/**
	 * Get literal value in a given class from text stored in given property
	 * @param classloader classloader used to load type
	 * @param objectsBeingAccessed cache of objects
	 * @param p property storing that value. It must be the direct property ! (Typically in case of collections, for which properties
	 * have indirections through LiteralInCollectionUpdaterProperty, which can be used here)
	 * @param propertyText text of property
	 * @return loaded literal
	 */
	public static Object getLiteralFromText(ClassLoader classloader, ObjectCache objectsBeingAccessed, Property p, String propertyText) {
		LiteralTransformer transformer = null;
		Class propertyClass = null;
		if(Literals.containsKey(p.getType())) {
			propertyClass = p.getType();
			transformer = Literals.get(p.getType());
		} else {
			// will be useful only for fields typed as Object or Serializable, as others will go in the if upper
			String propertyType = getTypePrefix(propertyText);
			propertyText = getValueIn(propertyText);
			propertyClass = (Class) Literals.classes.getTransformer().fromString(propertyType, Class.class, classloader, objectsBeingAccessed);
			transformer = Literals.get(classloader, propertyType);
		}
		Object returned = transformer.fromString(propertyText, propertyClass, classloader, objectsBeingAccessed);
		return returned;
	}
}
