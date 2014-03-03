package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Method;

public class MethodConstructedOnMissingField extends CantMatchMethodToCodeException {

	public MethodConstructedOnMissingField(Method method, String realFieldName, NoSuchFieldInHierarchyException e) {
		super("method "+method.toString()+" was apparently defined in informer "+method.getDeclaringClass().getName()+" " +
				"for field "+realFieldName+" which doesn't seems to exist", e);
	}

}
