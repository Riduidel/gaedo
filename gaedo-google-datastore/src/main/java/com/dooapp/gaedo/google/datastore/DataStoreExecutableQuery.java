package com.dooapp.gaedo.google.datastore;

import com.google.appengine.api.datastore.Entity;

/**
 * Interface to datastore queries shadowing the fact that the query can be either a map fetch of keys
 * or a real query
 * @author ndx
 *
 */
interface DataStoreExecutableQuery {
	/**
	 * Get entity count
	 * @return
	 */
	int count();

	/**
	 * Get all entities between start and end idnex
	 * @param start
	 * @param end
	 * @return
	 */
	Iterable<Entity> getAll(int start, int end);

	/**
	 * Get absolutely all entites
	 * @return
	 */
	Iterable<Entity> getAll();

	/**
	 * Get only one result
	 * @return
	 */
	Entity getEntity();
	
}