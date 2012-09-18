package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Allow transformation of a data type into a literal vertex
 * @author ndx
 *
 *@param Type type of transformed data (to avoid cast)
 */
public interface LiteralTransformer<Type> extends Transformer {
	/**
	 * Get expected vertex id for the given value
	 * @param value input value
	 * @return the id used for the vertex representing this value
	 */
	public String getVertexId(Type value);
	/**
	 * Get vertex for given object. This vertex may be simple or the root of a sub-graph
	 * @param database
	 * @param value
	 * @return
	 */
	Vertex getVertexFor(GraphDatabaseDriver database, Type value);
	
	/**
	 * Load given vertex into an object
	 * @param key
	 * @return
	 */
	public Type loadObject(GraphDatabaseDriver driver, Vertex key);
	/**
	 * Load given key into an object, with added bonus of known type
	 * @param driver TODO
	 * @param effectiveClass
	 * @param key
	 * @return
	 */
	public Type loadObject(GraphDatabaseDriver driver, Class effectiveClass, Vertex key);
	
	/**
	 * Method loading class in given classloader and the loading given object
	 * @param driver TODO
	 * @param classLoader used classloader
	 * @param effectiveType type name
	 * @param key vertex associated to value
	 * @return loaded object
	 * @see #loadObject(GraphDatabaseDriver, Class, Vertex)
	 */
	public Object loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, String effectiveType, Vertex key);
	
	/**
	 * Check if vertex content is equals to provided object
	 * @param driver TODO
	 * @param currentVertex currently analyzed vertex
	 * @param expected expected value
	 * @return true if vertex is supposed ton contain value, false otherwise
	 */
	public boolean isVertexEqualsTo(GraphDatabaseDriver driver, Vertex currentVertex, Type expected);

}
