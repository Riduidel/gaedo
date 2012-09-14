package com.dooapp.gaedo.blueprints.providers;

import com.dooapp.gaedo.blueprints.AbstractGraphProvider;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

public class Tinker extends AbstractGraphProvider implements GraphProvider {
	@Override
	public IndexableGraph get(String path) {
		return new TinkerGraph(path+"/tinker");
	}
}