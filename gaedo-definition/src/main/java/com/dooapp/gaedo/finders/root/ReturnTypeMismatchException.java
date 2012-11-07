package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Method;

import com.dooapp.gaedo.finders.FieldInformer;

/**
 * Exception thrown when return type of method declaration does not match expected return type
 * @author ndx
 *
 */
public class ReturnTypeMismatchException extends
		CantMatchMethodToCodeException {

	public ReturnTypeMismatchException(Method invoked,
			Class<? extends FieldInformer> realReturnType,
			Class<?> expectedReturnType) {
		super("There is a type mismatch between real return type "
				+ realReturnType.getName() + " and expected return type "
				+ expectedReturnType.getName()
				+ "\nOne should check declaration of method "
				+ invoked.toGenericString() + " and associated field");
	}

}