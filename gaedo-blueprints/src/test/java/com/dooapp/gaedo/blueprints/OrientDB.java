package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;

class OrientDB implements GraphProvider {
	@Override
	public IndexableGraph get() {
		return new OrientGraph("local:/"+GraphBackedLoadTest.GRAPH_DIR+"/orient.db");
	}
}