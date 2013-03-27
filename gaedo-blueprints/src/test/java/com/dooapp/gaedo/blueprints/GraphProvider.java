package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.IndexableGraph;

public interface GraphProvider {
	public static final String GRAPH_DIR = System.getProperty("user.dir")+"/target/tests/graph";

	/**
	 * Get a graph storing data in a subdir of path. Subdir is selected by implementation and should not be changed.
	 * @param path path where data is to be stored. Should ideally start with {@link #GRAPH_DIR}
	 * @return a working graph
	 */
	IndexableGraph get(String path);

	/**
	 * Provides a common name for all instances of that graph provider
	 * @return
	 */
	String getName();

	/**
	 * Get a path local to graph provider, from a given root.
	 * Obtained path should be similar to the one used in {@link #get(String)}
	 * @param usablePath
	 * @return
	 */
	String path(String usablePath);
}