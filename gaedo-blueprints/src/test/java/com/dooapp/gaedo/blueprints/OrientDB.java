package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;

/**
 * Do not use it : transaction support seems awkward now
 * @author ndx
 *
 */
@Deprecated
class OrientDB implements GraphProvider {
	@Override
	public IndexableGraph get() {
		OrientGraph returned = new OrientGraph("local:/"+GraphBackedLoadTest.GRAPH_DIR+"/orient.db");
		return returned;
	}
}