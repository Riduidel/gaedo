package com.dooapp.gaedo.blueprints.queries.executable;

import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Executable query performing the search/sort and returing corresponding vertices
 * @author ndx
 *
 */
public interface GraphExecutableQuery {

	/**
	 * Count the number of vertices matching query criterias
	 * @return
	 */
	int count();

	/**
	 * Get a subset of matching query results
	 * @param start
	 * @param end
	 * @return iterable allowing browsing of those vertices
	 */
	Iterable<Vertex> get(int start, int end);

	/**
	 * Get all result vertices matching query results
	 * @return
	 */
	Iterable<Vertex> getAll();

	/**
	 * Get first vertex matching query
	 * @return
	 */
	Vertex getVertex();

}
