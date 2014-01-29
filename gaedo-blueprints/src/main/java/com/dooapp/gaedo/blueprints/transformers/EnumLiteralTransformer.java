package com.dooapp.gaedo.blueprints.transformers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.ObjectCache.ValueLoader;
import com.tinkerpop.blueprints.Vertex;

public class EnumLiteralTransformer extends AbstractLiteralTransformer<Enum> implements LiteralTransformer<Enum> {
	private static final Logger logger = Logger.getLogger(EnumLiteralTransformer.class.getName());

	@Override
	public String valueToString(Enum value) {
		return value.name();
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		try {
			return Enum.class.isAssignableFrom(GraphUtils.loadClass(classLoader, effectiveType));
		} catch (ClassNotFoundException e) {
//			throw new UnsupportedOperationException("class "+effectiveType+" isn't considered to be an enum ... Maybe is there any classloader issue hidden there", e);
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "is "+effectiveType+" really not an enum ???");
			}
			return false;
		}
	}

	public boolean areEquals(Object expected, String effectiveGraphValue) {
		if(Enum.class.isAssignableFrom(expected.getClass()))
			return toString(Enum.class.cast(expected)).equals(effectiveGraphValue);
		return false;
	}
}
