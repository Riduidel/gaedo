package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.Properties;
import com.tinkerpop.blueprints.pgm.Vertex;

public class ClassLiteralTransformer extends AbstractLiteralTransformer<Class> implements LiteralTransformer<Class> {
	@Override
	protected Object getVertexValue(Class value) {
		return value.getCanonicalName();
	}

}
