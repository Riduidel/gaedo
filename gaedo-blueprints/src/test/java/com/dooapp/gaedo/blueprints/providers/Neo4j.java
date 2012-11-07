package com.dooapp.gaedo.blueprints.providers;

import com.dooapp.gaedo.blueprints.AbstractGraphProvider;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

public class Neo4j extends AbstractGraphProvider implements GraphProvider {
	@Override
	public IndexableGraph get(String path) {
		Neo4jGraph neo4jgraph = new Neo4jGraph(path(path));
		neo4jgraph.setMaxBufferSize(0);
		return neo4jgraph;
	}

	@Override
	public String path(String usablePath) {
		return usablePath+"/neo4j/";
	}
}