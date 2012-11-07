package com.dooapp.gaedo.utils;

import java.lang.reflect.Method;

/**
 * Resolves methods call by calling given method on given object with the input parameters
 * @author ndx
 *
 */
public class CallMethodOnObjectResolver implements MethodResolver {
	/**
	 * Called method
	 */
	protected final Method method;

	/**
	 * Target object
	 */
	protected final Object object;

	public CallMethodOnObjectResolver(Object object, Method method) {
		super();
		this.method = method;
		this.object = object;
	}

	@Override
	public Object call(Object[] invokedArgs) throws Throwable {
		return method.invoke(object, invokedArgs);
	}

}
