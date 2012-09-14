package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

public class Tinker extends AbstractGraphProvider implements GraphProvider {
	@Override
	public IndexableGraph get(String path) {
		return new TinkerGraph(path+"/tinker");
	}
}