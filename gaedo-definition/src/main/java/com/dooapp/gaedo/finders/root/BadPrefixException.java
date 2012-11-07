package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Method;

/**
 * A bad prefix exception is thrown when the method don't start with {value
 * InformerClassInvocationHandler#SYNTHETIC_GETTER_PREFIX}. To avoid this
 * exception, change your method code to start with this prefix.
 * 
 * @author Nicolas
 * 
 */
public class BadPrefixException extends
		CantMatchMethodToCodeException {

	public BadPrefixException(Method invoked) {
		super("A method that can be mapped to code MUST start with "
				+ InformerClassInvocationHandler.SYNTHETIC_GETTER_PREFIX
				+ " prefix. The one you wrote does NOT :\n"
				+ invoked.toGenericString());
	}

	public BadPrefixException(String invoked) {
		super("A method that can be mapped to code MUST start with "
				+ InformerClassInvocationHandler.SYNTHETIC_GETTER_PREFIX
				+ " prefix. The one you wrote does NOT :\n"
				+ invoked+"(...)");
	}
}