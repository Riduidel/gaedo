/**
 * 
 */
package com.dooapp.gaedo.utils;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Thrown when {@link Class#newInstance()} cannot be correctly called on given class, or when input key is not compatible with service associated kind
 * @author ndx
 *
 */
public class UnableToCreateObjectException extends CrudServiceException {
	public UnableToCreateObjectException(Exception e, Class<?> containedClass) {
		super("unable to create object of class "+containedClass.getName()+" maybe there is no empty constructor ...", e);
	}
	
	protected UnableToCreateObjectException(String s) {
		super(s);
	}
}