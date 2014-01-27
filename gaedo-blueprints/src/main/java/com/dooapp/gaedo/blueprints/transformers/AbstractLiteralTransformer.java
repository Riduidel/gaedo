package com.dooapp.gaedo.blueprints.transformers;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.ObjectCache.ValueLoader;
import com.dooapp.gaedo.blueprints.UnableToCreateException;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Vertex;

/**
 * Helper class for literal transformer
 *
 * @author ndx
 *
 */
public abstract class AbstractLiteralTransformer<Type> {

	/**
	 * Get vertex for the given value. Vertex is first searched using its id (an
	 * id which really looks like vertex value - disambiguated by its type
	 *
	 * @param driver
	 * @param value
	 * @return
	 * @category vertex
	 * @deprecated this was useful in version 0.* of gaedo, but has been
	 *             replaced by
	 *             {@link #setPropertyFor(Object, Vertex, Property, Object)}
	 */
	public Vertex getVertexFor(GraphDatabaseDriver driver, Type value, CascadeType cascade) {
		String vertexId = toString(value);
		Vertex returned = driver.loadVertexFor(vertexId, value.getClass().getName());
		// If vertex doesn't exist (and cascade allow its creation) ... load it
		// !
		if (returned == null && GraphUtils.canCreateVertex(cascade)) {
			returned = driver.createEmptyVertex(value.getClass(), vertexId, value);
			driver.setValue(returned, toString(value));
		}
		return returned;
	}

	/**
	 * Get property value for the given property and vertex
	 *
	 * @param objectVertex
	 * @param valueClass
	 * @param classloader
	 * @param objectCache
	 *            TODO
	 * @return
	 */
	public Type fromString(String propertyValue, Class valueClass, ClassLoader classloader, ObjectCache objectCache) {
		if(propertyValue.startsWith(valueClass.getName())) {
			return internalLoadObject(valueClass, Literals.getValueIn(propertyValue), objectCache);
		} else {
			return internalLoadObject(valueClass, propertyValue, objectCache);
		}
	}

	/**
	 * Load object from vertex
	 *
	 * @param driver
	 *            database driver
	 * @param key
	 *            vertex in which value is stored
	 * @return loaded object
	 * @see #internalLoadObject(GraphDatabaseDriver, Class, Vertex)
	 * @category vertex
	 * @deprecated this was useful in version 0.* of gaedo, but has been
	 *             replaced by
	 *             {@link #getPropertyFor(Vertex, Property, ClassLoader, ObjectCache)}
	 */
	public Type loadObject(GraphDatabaseDriver driver, Vertex key) {
		String effectiveType = driver.getEffectiveType(key);
		try {
			Class valueClass = Class.forName(effectiveType);
			return loadObjectFromVertex(driver, valueClass, key, null);
		} catch (ClassNotFoundException e) {
			throw UnableToCreateException.dueTo(key, effectiveType, e);
		}
	}

	public Object loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, String effectiveType, Vertex key, ObjectCache objectCache) {
		try {
			Class<?> loadedClass = GraphUtils.loadClass(classLoader, effectiveType);
			return loadObjectFromVertex(driver, loadedClass, key, objectCache);
		} catch (ClassNotFoundException e) {
			throw UnableToCreateException.dueTo(key, effectiveType, e);
		}
	}

	/**
	 * Resolve type to a loadable one
	 *
	 * @param effectiveType
	 * @return
	 */
	protected abstract String resolveType(String effectiveType);

	/**
	 * Load object of given class
	 *
	 * @param driver
	 *            database driver
	 * @param valueClass
	 *            class this object should be an instance of
	 * @param key
	 *            vertex in which value is stored
	 * @param objectCache
	 *            used cache. THIS FIELD MAY BE NULL
	 * @return loaded object
	 * @see #internalLoadObject(Class, Vertex, String)
	 * @category vertex
	 * @deprecated this was useful in version 0.* of gaedo, but has been
	 *             replaced by
	 *             {@link #getPropertyFor(Vertex, Property, ClassLoader, ObjectCache)}
	 */
	protected Type loadObjectFromVertex(GraphDatabaseDriver driver, Class valueClass, Vertex key, ObjectCache objectCache) {
		String valueString = driver.getValue(key).toString();
		return internalLoadObject(valueClass, valueString, objectCache);
	}

	/**
	 * Load object from given informations
	 *
	 * @param valueClass
	 *            class this object should be an instance of
	 * @param valueString
	 *            value representing the object to load
	 * @param objectCache
	 *            used cache. THIS FIELD MAY BE NULL
	 * @return loaded object
	 */
	protected Type internalLoadObject(final Class valueClass, final String valueString, ObjectCache objectCache) {
		ValueLoader loader = createValueLoader(valueClass, valueString);
		if (objectCache == null)
			return (Type) loader.get();
		return (Type) objectCache.get(getObjectCacheId(valueClass, valueString), loader);
	}

	/**
	 * Get cache id for this value
	 *
	 * @param valueClass
	 * @param valueString
	 * @return
	 */
	protected String getObjectCacheId(final Class valueClass, final String valueString) {
		return valueClass.getName() + ":" + valueString;
	}

	protected ValueLoader createValueLoader(final Class valueClass, final String valueString) {
		return new ValueLoader() {

			@Override
			public Object get() {
				return loadValueFromString(valueClass, valueString);
			}
		};
	}

	/**
	 * Load value from the given string. This method should be the only one allowed to load value for a literal.
	 * @param valueClass
	 * @param valueString
	 * @return loaded value
	 * @see Utils#fromString(String, Class) default implementation delegates work to this method
	 */
	protected Type loadValueFromString(final Class valueClass, final String valueString) {
		return (Type) Utils.fromString(valueString, valueClass);
	}

	/**
	 * Get vertex value for object. It's this value that will be used in graph.
	 * Notice this value MUST include a class prefix, as it is possible the value is refered using an Objet or Serializable or Number generic class.
	 *
	 * @param value
	 * @return a value for that vertex. Again, null is NOT allowed.
	 */
	public final String toString(Type value) {
		return typeToString((Class<? extends Type>) value.getClass())+Literals.CLASS_VALUE_SEPARATOR+valueToString(value);
	}

	protected String typeToString(Class<? extends Type> valueClass) {
		return valueClass.getName();
	}

	protected abstract String valueToString(Type value);

	public String getTypeOf(Object value) {
		return TypeUtils.getType(value.getClass());
	}
}
