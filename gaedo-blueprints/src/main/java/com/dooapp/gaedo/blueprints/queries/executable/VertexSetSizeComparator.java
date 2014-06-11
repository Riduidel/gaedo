package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Comparator;

/**
 * Compare vertex set first by set size, then by property path depth, then finally by loaded vertices list
 * @author ndx
 *
 */
class VertexSetSizeComparator implements Comparator<VertexSet> {

	@Override
	public int compare(VertexSet o1, VertexSet o2) {
		int returned = (int) Math.signum(o1.size()-o2.size());
		if(returned==0) {

		}
		return returned;
	}

}