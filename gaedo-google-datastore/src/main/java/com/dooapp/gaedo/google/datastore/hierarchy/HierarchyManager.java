package com.dooapp.gaedo.google.datastore.hierarchy;

import com.google.appengine.api.datastore.Key;

/**
 * Interface designed to handle hierarchical links between object graphs. An
 * object implementing this interface is responsible for handling hierarchy
 * between a class managed by a datastore finder service. As a consequence, this
 * interface defines how relationships between this class and its supposed
 * parent/children can be handled.
 * 
 * @author Nicolas
 * 
 */
public interface HierarchyManager {
	/**
	 * 
	 * @return true if objects of managed class are supposed to have parents
	 */
	boolean hasParent();

	/**
	 * Get parent key for an object of type managed by this hierarchy manager
	 * @param data
	 * @return
	 */
	Key getParentKey(Object data);

}
