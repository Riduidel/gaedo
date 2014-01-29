package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.ObjectCache.ValueLoader;
import com.dooapp.gaedo.properties.TypeProperty;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Vertex;


public class ClassLiteralTransformer extends AbstractSimpleLiteralTransformer<Class> implements LiteralTransformer<Class> {

	public ClassLiteralTransformer() {
		super(Class.class);
	}
	@Override
	public String valueToString(Class value) {
		return value==null ? Object.class.getCanonicalName() : value.getCanonicalName();
	}

	public boolean areEquals(Object expected, String effectiveGraphValue) {
		if(type.isAssignableFrom(expected.getClass()))
			return toString(type.cast(expected)).equals(effectiveGraphValue);
		if(expected instanceof String)
			return expected.equals(ClassIdentifierHelper.getValueIn(effectiveGraphValue));
		return false;
	}

	public Class fromString(String propertyValue, Class valueClass, ClassLoader classloader, ObjectCache objectCache) {
		try {
			String classValue = Utils.maybeObjectify(propertyValue);
			return classloader.loadClass(classValue);
		} catch (ClassNotFoundException e) {
			throw new BadLiteralException("unable to load class \""+propertyValue+"\" using given classloader");
		}
	}
}
