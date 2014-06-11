package com.dooapp.gaedo.blueprints.queries.executable;

import com.tinkerpop.blueprints.Vertex;

/**
 * Interface allowing one to obtain count of vertices without having to load them. This is specially convenient for queries involving indices.
 * @author ndx
 *
 */
public interface LazyLoader extends Comparable<LazyLoader>{
	/**
	 * Obtain list of vertices for this lazy loader
	 * @return
	 */
	Iterable<Vertex> get();

	/**
	 * Obtain number of vertices this lazy loader will load
	 * @return
	 */
	long size();

	/**
	 * Dive through this lazy loader to the set of loaded vertices.
	 * @return
	 */
	LazyLoader diveIntoLoadedSet();

}