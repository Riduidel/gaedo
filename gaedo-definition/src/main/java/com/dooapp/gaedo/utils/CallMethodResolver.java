package com.dooapp.gaedo.utils;

import java.lang.reflect.Method;

/**
 * Resolves call by executing the given callMethod method
 * @author ndx
 *
 */
public abstract class CallMethodResolver implements MethodResolver {
	protected final Method method;

	public CallMethodResolver(Method method) {
		super();
		this.method = method;
	}

	@Override
	public Object call(Object[] invokedArgs) throws Throwable {
		return callMethod(method, invokedArgs);
	}

	/**
	 * Effective method call with given method and parameters
	 * @param method method to call
	 * @param invokedArgs used parameters
	 * @return method result ... what else ?
	 */
	protected abstract Object callMethod(Method method, Object[] invokedArgs) throws Throwable;

}
