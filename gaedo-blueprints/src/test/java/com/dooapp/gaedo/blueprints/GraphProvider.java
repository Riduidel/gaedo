package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;

public interface GraphProvider {
	public static final String GRAPH_DIR = System.getProperty("user.dir")+"/target/tests/graph";

	/**
	 * Get a graph storing data in a subdir of path. Subdir is selected by implementation and should not be changed.
	 * @param path path where data is to be stored. Should ideally start with {@link #GRAPH_DIR}
	 * @return a working graph
	 */
	IndexableGraph get(String path);
}