package com.dooapp.gaedo.blueprints.providers;

import java.io.File;

import com.dooapp.gaedo.blueprints.AbstractGraphProvider;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class Tinker extends AbstractGraphProvider implements GraphProvider {
	@Override
	public IndexableGraph get(String path) {
		String fullPath = path(path);
		File f = new File(fullPath);
		f.getParentFile().mkdirs();
		return new TinkerGraph(f.getAbsolutePath());
	}

	@Override
	public String path(String path) {
		return path+"/tinker/";
	}
}