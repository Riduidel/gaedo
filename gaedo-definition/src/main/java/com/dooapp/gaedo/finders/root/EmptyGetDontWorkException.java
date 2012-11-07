package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Method;

/**
 * Writing an empty get shouldn't work
 * 
 * @author Nicolas
 * 
 */
public class EmptyGetDontWorkException extends
		CantMatchMethodToCodeException {

	public EmptyGetDontWorkException(Method invoked) {
		super(
				"The empty get method can't be mapped o an existing field (in other words, the field named \"\" exists nowhere else than in your wettest dreams) :\n"
						+ invoked.toGenericString());
	}

	public EmptyGetDontWorkException(String invoked) {
		super(
				"The empty get method can't be mapped o an existing field (in other words, the field named \"\" exists nowhere else than in your wettest dreams) :\n"
						+ invoked+"(...)");
	}
}