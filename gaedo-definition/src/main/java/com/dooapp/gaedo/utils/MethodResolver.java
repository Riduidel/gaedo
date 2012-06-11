package com.dooapp.gaedo.utils;

/**
 * Interface defining how a method can be resolved to a call
 * @author ndx
 *
 */
public interface MethodResolver {

	public abstract Object call(Object[] invokedArgs) throws Throwable;

}
