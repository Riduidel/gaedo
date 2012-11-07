package com.dooapp.gaedo.patterns;

/**
 * A lazy loadable object can be loaded or not.
 * @author ndx
 *
 */
public interface LazyLoadable {
	/**
	 * @return true if object content has effectively been loaded
	 */
	boolean isLoaded();

}
