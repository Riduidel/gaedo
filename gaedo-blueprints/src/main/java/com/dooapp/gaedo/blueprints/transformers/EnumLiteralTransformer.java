package com.dooapp.gaedo.blueprints.transformers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;

public class EnumLiteralTransformer extends AbstractLiteralTransformer<Enum> implements LiteralTransformer<Enum> {
	private static final Logger logger = Logger.getLogger(EnumLiteralTransformer.class.getName());

	@Override
	protected Object getVertexValue(Enum value) {
		return value.name();
	}

	@Override
	protected Class getValueClass(Enum value) {
		return value==null ? Enum.class : value.getClass();
	}

	@Override
	protected String resolveType(String effectiveType) {
		return effectiveType;
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

	@Override
	public Kind getKind() {
		return Kind.bnode;
	}
}
