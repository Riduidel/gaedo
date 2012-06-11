package com.dooapp.gaedo.properties;

import java.beans.IntrospectionException;

/**
 * Thrown when trying to build a bean property from a non-bean object
 * @author ndx
 *
 */
public class NotABeanException extends PropertyProviderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotABeanException(Class<?> containedClass, IntrospectionException e) {
		super("unable to build properties from "+containedClass.toString()+" as it not appears to be a valid Java Bean", e);
	}
	
}