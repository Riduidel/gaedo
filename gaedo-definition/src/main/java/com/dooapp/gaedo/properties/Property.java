package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * A property is our vision of what a bean attribute is.
 * Notice implementors of this interface must implement equals and hashcode for semantic equality
 * @author ndx
 *
 */
public interface Property {
	/**
	 * Provides a view, including generics element, of this property
	 * @return
	 */
	String toGenericString();

	/**
	 * Get value of property for the given bean. Notice this getter may not work the same way a normal getter would.
	 * @param source
	 * @return
	 */
	Object get(Object bean);
	
	/**
	 * Set value of property for the given bean
	 * @param bean input bean
	 * @param value input value
	 */
	void set(Object bean, Object value);

	/**
	 * Get this property name
	 * @category getter
	 * @return
	 */
	String getName();

	/**
	 * Get generic type of this property
	 * @category getter
	 * @return
	 */
	Type getGenericType();

	/**
	 * Get class where the property is declared
	 * @category getter
	 * @return
	 */
	Class<?> getDeclaringClass();

	/**
	 * Non generic view of property type
	 * @return
	 */
	Class<?> getType();

	/**
	 * get value of a given annotation class for this property
	 * @param class1
	 * @return
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * Get all of this property annotations
	 * @return
	 */
	public Collection<? extends Annotation> getAnnotations();

	/**
	 * Check if property has the given modifier
	 * @param static1
	 * @return
	 */
	boolean hasModifier(int modifier);

	/**
	 * Convert field from a string input. usually, this input is a toString view of that property
	 * @param value
	 * @return
	 */
	Object fromString(String value);
}
