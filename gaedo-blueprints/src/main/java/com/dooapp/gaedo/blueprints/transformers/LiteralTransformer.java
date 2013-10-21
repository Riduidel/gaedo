package com.dooapp.gaedo.blueprints.transformers;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.tinkerpop.blueprints.Vertex;

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
	 * @param cascade cascade type. if nor PERSIST neither MERGE and vertex doesn't exist, null may be returned
	 * @param cascade
	 * @return
	 */
	Vertex getVertexFor(GraphDatabaseDriver database, Type value, CascadeType cascade);

	/**
	 * Load given vertex into an object. Notice this method is mainly to be used during search (and particularly to identify matching vertices)
	 * @param key
	 * @return
	 */
	public Type loadObject(GraphDatabaseDriver driver, Vertex key);

	/**
	 * Method loading class in given classloader and the loading given object
	 * @param driver TODO
	 * @param classLoader used classloader
	 * @param effectiveType type name
	 * @param key vertex associated to value
	 * @param objectsBeingAccessed
	 * @return loaded object
	 * @see #loadObject(GraphDatabaseDriver, Class, Vertex)
	 */
	public Object loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, String effectiveType, Vertex key, ObjectCache objectsBeingAccessed);

	/**
	 * Check if vertex content is equals to provided object
	 * @param driver TODO
	 * @param currentVertex currently analyzed vertex
	 * @param expected expected value
	 * @return true if vertex is supposed ton contain value, false otherwise
	 */
	public boolean isVertexEqualsTo(GraphDatabaseDriver driver, Vertex currentVertex, Type expected);
	/**
	 * @return Kind of object to associate to the literals managed by this transformer. Used to separate Classes (which are fake literals) from others
	 */
	public Kind getKind();

	/**
	 * Get graph type of the vertex associated to that value
	 * @param value value to get type of
	 * @return the graph type of that value. Most often a direct call to {@link TypeUtils} will be done
	 */
	public String getTypeOf(Object value);

}
