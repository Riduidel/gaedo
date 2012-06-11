package com.dooapp.gaedo.exceptions.finder.dynamic;

import java.lang.reflect.Method;
import java.util.Collection;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.exceptions.DynamicFinderException;
import com.dooapp.gaedo.finders.dynamic.DynamicFinderMethodResolver;

/**
 * Exception sent went a method defined in service dynamic interface cannot be bound to a set of methods from real service interface
 * @author ndx
 *
 */
public class MethodBindingException extends DynamicFinderException {
	/**
	 * Build message used for exception from various fragments
	 * @param method
	 * @param methodResolver
	 * @param errors
	 * @return
	 */
	private static String buildMessage(Method method, DynamicFinderMethodResolver methodResolver, Collection<String> errors) {
		StringBuilder sOut = new StringBuilder();
		sOut.append("unable to resolve call to ").append(method.toGenericString()).append("\n");
		sOut.append("it should have been resolved using\n").append(methodResolver.toString()).append("\n");
		sOut.append("unfortunatly, the following errors appeared\n");
		sOut.append(listErrors(errors));
		return sOut.toString();
	}

	public MethodBindingException(Method method,
			DynamicFinderMethodResolver methodResolver, Collection<String> errors) {
		super(buildMessage(method, methodResolver, errors));
	}
	
}