package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;

/**
 * Do not use it : transaction support seems awkward now
 * @author ndx
 *
 */
@Deprecated
public class OrientDB implements GraphProvider {
	@Override
	public IndexableGraph get(String path) {
		OrientGraph returned = new OrientGraph("local:/"+path+"/orient.db");
		return returned;
	}
}