package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

class Tinker implements GraphProvider {
	@Override
	public IndexableGraph get() {
		return new TinkerGraph(GraphBackedLoadTest.GRAPH_DIR+"/tinker");
	}
}