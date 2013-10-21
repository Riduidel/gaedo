package com.dooapp.gaedo.blueprints.transformers;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.ObjectCache.ValueLoader;
import com.dooapp.gaedo.blueprints.UnableToCreateException;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Vertex;

/**
 * Helper class for literal transformer
 * @author ndx
 *
 */
public abstract class AbstractLiteralTransformer<Type> {

    /**
     * Get vertex for the given value.
     * Vertex is first searched using its id (an id which really looks like vertex value - disambiguated by its type
     * @param driver
     * @param value
     * @return
     */
    public Vertex getVertexFor(GraphDatabaseDriver driver, Type value, CascadeType cascade) {
        String vertexId = getVertexId(value);
        Vertex returned = driver.loadVertexFor(vertexId, value.getClass().getName());
        // If vertex doesn't exist (and cascade allow its creation) ... load it !
        if(returned==null && GraphUtils.canCreateVertex(cascade)) {
            returned = driver.createEmptyVertex(value.getClass(), vertexId, value);
            driver.setValue(returned, getVertexValue(value));
        }
        return returned;
    }

    /**
     * Load object from vertex
     * @param driver database driver
     * @param key vertex in which value is stored
     * @return loaded object
     * @see #internalLoadObject(GraphDatabaseDriver, Class, Vertex)
     */
    public Type loadObject(GraphDatabaseDriver driver, Vertex key) {
        String effectiveType = driver.getEffectiveType(key);
        try {
            Class valueClass = Class.forName(effectiveType);
            return internalLoadObject(driver, valueClass, key, null);
        } catch (ClassNotFoundException e) {
            throw UnableToCreateException.dueTo(key, effectiveType, e);
        }
    }

    public Object loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, String effectiveType, Vertex key, ObjectCache objectCache) {
        try {
            Class<?> loadedClass = GraphUtils.loadClass(classLoader, effectiveType);
            return internalLoadObject(driver, loadedClass, key, objectCache);
        } catch (ClassNotFoundException e) {
            throw UnableToCreateException.dueTo(key, effectiveType, e);
        }
    }

    /**
     * Resolve type to a loadable one
     * @param effectiveType
     * @return
     */
    protected abstract String resolveType(String effectiveType);

    /**
     * Load object of given class
     * @param driver database driver
     * @param valueClass class this object should be an instance of
     * @param key vertex in which value is stored
     * @param objectCache used cache. THIS FIELD MAY BE NULL
     * @return loaded object
     * @see #internalLoadObject(Class, Vertex, String)
     */
    protected Type internalLoadObject(GraphDatabaseDriver driver, Class valueClass, Vertex key, ObjectCache objectCache) {
        String valueString = driver.getValue(key).toString();
        return internalLoadObject(valueClass, key, valueString, objectCache);
    }

    /**
     * Load object from given informations
     * @param valueClass class this object should be an instance of
     * @param key vertex in which value is stored
     * @param valueString value representing the object to load
     * @param objectCache used cache. THIS FIELD MAY BE NULL
     * @return loaded object
     */
    protected Type internalLoadObject(final Class valueClass, Vertex key, final String valueString, ObjectCache objectCache) {
    	ValueLoader loader = createValueLoader(valueClass, valueString);
		if(objectCache==null)
			return (Type) loader.get();
    	return (Type) objectCache.get(getObjectCacheId(valueClass, valueString), loader);
    }

    /**
     * Get cache id for this value
     * @param valueClass
     * @param valueString
     * @return
     */
	protected String getObjectCacheId(final Class valueClass, final String valueString) {
		return valueClass.getName()+":"+valueString;
	}

	protected ValueLoader createValueLoader(final Class valueClass, final String valueString) {
		return new ValueLoader() {

			@Override
			public Object get() {
	    		return (Type) Utils.fromString(valueString, valueClass);
			}
		};
	}

    /**
     * Get vertex value for object. notice it is better to get it as a string than anything else
     * @param value
     * @return a value for that vertex. Again, null is NOT allowed.
     */
    protected abstract Object getVertexValue(Type value);

    /**
     * Creates an id out of an object
     * @param value
     * @return
     */
    public String getVertexId(Type value) {
        return /* getValueClass(value).getCanonicalName()+":"+*/ getVertexValue(value).toString();
    }

    /**
     * Get value class name for this literal
     * @param value
     * @return usually should return value.getClass(). The main thing to understand is that null is NOT allowed
     */
    protected abstract Class getValueClass(Type value);

    public boolean isVertexEqualsTo(GraphDatabaseDriver driver, Vertex currentVertex, Type expected) {
        return ((expected==null && currentVertex==null) ||
                        (expected!=null && getVertexValue(expected).equals(driver.getValue(currentVertex))));
    }

    public Kind getKind() {
        return Kind.literal;
    }

    public String getTypeOf(Object value) {
        return TypeUtils.getType(value.getClass());
    }
}
