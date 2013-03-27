package com.dooapp.gaedo.blueprints.indexable;

import java.util.LinkedList;
import java.util.List;

import com.dooapp.gaedo.blueprints.Properties;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

public class PropertyUtils {

	public static List<Class<? extends Element>> appliesTo(Properties p) {
		List<Class<? extends Element>> returned = new LinkedList<Class<? extends Element>>();
		switch(p) {
		case collection_index:
		case label:
			returned.add(Edge.class);
			break;
		case kind:
		case type:
		case value:
			returned.add(Vertex.class);
			break;
		}
		return returned;
		
	}

}
