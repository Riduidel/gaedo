package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;

class Neo4j implements GraphProvider {
	@Override
	public IndexableGraph get() {
		Neo4jGraph neo4jgraph = new Neo4jGraph(GraphBackedLoadTest.GRAPH_DIR+"/neo4j");
		neo4jgraph.setMaxBufferSize(0);
		return neo4jgraph;
	}
}