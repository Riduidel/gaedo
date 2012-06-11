package com.dooapp.gaedo.prevalence.space;

import java.io.Serializable;
import java.util.Iterator;

/**
 * A storage space is the root storage element of a prevalence layer. All data
 * resides in that storage space. To a certain extend, this storage space can be
 * seen as a map of containers, those containers being maps, collections, or
 * simple objects.
 * 
 * Notice most of this interface (with the notable exception of Key
 * parameterized type) has been borrowed from excellent space4j Space interface
 * (see its javadoc at http://www.space4j.org/api/org/space4j/Space.html)
 * 
 * @author ndx
 * 
 * @param <Key> used key type, allow simpler coercion over entered keys
 */
public interface StorageSpace<Key extends Serializable> extends Serializable {
	/**
	 * Check if this storage space contains the given key
	 * 
	 * @param key
	 * @return true if in space, false elsewhere
	 * @see java.util.Collection#contains(Object)
	 * @see org.space4j.Space#check(Object)
	 */
	boolean contains(Key key);

	/**
	 * Get object associated to given key. It can be any kind of data
	 * 
	 * @param key
	 * @return
	 */
	Object get(Key key);

	/**
	 * Set value for given key
	 * @param key used key
	 * @param obj associated object
	 * @return previous value associated to key
	 */
	Object put(Key key, Object obj);

	/**
	 * Remove data associated to given key
	 * @param key input key
	 * @return previous content of key
	 */
	Object remove(Key key);
}
