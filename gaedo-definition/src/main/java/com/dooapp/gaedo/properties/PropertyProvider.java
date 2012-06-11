package com.dooapp.gaedo.properties;

/**
 * Class providing properties using various query mechanisms from an input class
 * @author ndx
 *
 */
public interface PropertyProvider {
	/**
	 * Get an array of properties declared only in that class.
	 * @param containedClass input class
	 * @return properties of this class
	 */
	Property[] get(Class<?> containedClass);
}
