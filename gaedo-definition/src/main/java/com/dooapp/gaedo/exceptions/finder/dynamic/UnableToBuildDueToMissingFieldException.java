package com.dooapp.gaedo.exceptions.finder.dynamic;

import java.lang.reflect.Method;
import java.util.Set;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.exceptions.DynamicFinderException;

/**
 * Method thrown when consumableText cannot be resolved in a method call due to a field used in method name that is not declared in service declared data class
 * @author ndx
 *
 */
public class UnableToBuildDueToMissingFieldException extends DynamicFinderException {

	public UnableToBuildDueToMissingFieldException(String consumableText, Method method, Set<String> set) {
		super("a part \""+consumableText+"\" of the method name you wrote \""+method.toGenericString()+"\" cannot be bound to any existing field.\n" +
				"assumed usable field names are "+set.toString());
	}
	

	public UnableToBuildDueToMissingFieldException(String consumableText, String methodName, Set<String> set) {
		super("a part \""+consumableText+"\" of the method name you wrote \""+methodName+"\" cannot be bound to any existing field.\n" +
				"assumed usable field names are "+set.toString());
	}
}