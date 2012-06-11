package com.dooapp.gaedo.exceptions.finder.dynamic;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.exceptions.DynamicFinderException;
import com.dooapp.gaedo.finders.dynamic.Mode;

/**
 * Exception thrown when given mode is not one of {@link Mode}
 * @author ndx
 *
 */
public class UnableToBuildDueToMissingModeException extends DynamicFinderException {

	public UnableToBuildDueToMissingModeException(Method method) {
		super("a part of the method you wrote \""+method.toGenericString()+"\" cannot be bound to any existing mode." +
				"\nexisting Mode values are "+Arrays.toString(Mode.values()));
	}

	public UnableToBuildDueToMissingModeException(String methodName) {
		super("a part of the method you wrote \""+methodName+"\" cannot be bound to any existing mode." +
				"\nexisting Mode values are "+Arrays.toString(Mode.values()));
	}

}
