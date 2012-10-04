package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.UnableToCreateException;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.pgm.Vertex;

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
	public Vertex getVertexFor(GraphDatabaseDriver driver, Type value) {
		String vertexId = getVertexId(value);
		Vertex returned = driver.loadVertexFor(vertexId, value.getClass().getName());
		// If vertex doesn't exist ... load it !
		if(returned==null) {
			returned = driver.createEmptyVertex(value.getClass(), vertexId);
			driver.setValue(returned, getVertexValue(value));
		}
		return returned;
	}

	/**
	 * Load object from vertex
	 * @param driver database driver
	 * @param key vertex in which value is stored
	 * @return loaded object
	 * @see #loadObject(GraphDatabaseDriver, Class, Vertex)
	 */
	public Type loadObject(GraphDatabaseDriver driver, Vertex key) {
		String effectiveType = driver.getEffectiveType(key);
		try {
			Class valueClass = Class.forName(effectiveType);
			return loadObject(driver, valueClass, key);
		} catch (ClassNotFoundException e) {
			throw new UnableToCreateException(effectiveType, e);
		}
	}

	public Object loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, String effectiveType, Vertex key) {
		try {
			Class<?> loadedClass;
			loadedClass = classLoader.loadClass(resolveType(effectiveType));
			return loadObject(driver, loadedClass, key);
		} catch (ClassNotFoundException e) {
			throw new UnableToCreateException(effectiveType, e);
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
	 * @return loaded object
	 * @see #loadObject(Class, Vertex, String)
	 */
	public Type loadObject(GraphDatabaseDriver driver, Class valueClass, Vertex key) {
		String valueString = driver.getValue(key).toString();
		return loadObject(valueClass, key, valueString);
	}

	/**
	 * Load object from given informations
	 * @param valueClass class this object should be an instance of
	 * @param key vertex in which value is stored
	 * @return loaded object
	 * @param valueString value representing the object to load
	 */
	protected Type loadObject(Class valueClass, Vertex key, String valueString) {
		return (Type) Utils.fromString(valueString, valueClass);
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
}
