package com.dooapp.gaedo.finders.dynamic;

import java.lang.reflect.Method;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Exception thrown when method return type does not complies to requirements of used mode
 * @author ndx
 *
 */
public class BadReturnTypeException extends CrudServiceException {

	public BadReturnTypeException(Method method, Class<?> returnType,
			Class<?> ...correctClasses) {
		super("return type of method "+method.toGenericString()+" is not good ! it is "+returnType.toString()+" when it could have only be one of "+correctClasses.toString());
	}

}
