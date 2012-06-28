package com.dooapp.gaedo.blueprints;

/**
 * Provides given object when {@link #get()} is called, then release it when {@link #release()} is.
 * Notice implementors are free to allow (or not) multiple calls to get to provide the same instance, while implementors should guarantee that same 
 * number of {@link #release()} than the number of {@link #get()} will release the object provided by this {@link Provider}.
 * 
 * @author ndx
 *
 * @param <T>
 */
public interface Provider<T> {
	public T get();
	
	public void release();
}
