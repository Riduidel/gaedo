package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Graph database interface allows classical operations on graph, viewed as a database : finding vertices, updating them, and so on ...
 * For those knowing old versions of gaedo-blueprints (0.2 branch), it's the equivalent of the GraphUtils class.
 * @author ndx
 *
 */
public interface GraphDatabaseDriver {
	/**
	 * Load vertex having the given id.
	 * Relationship between id and vertex is dependant upon graph implementation : id may be stored as a property, or it can be an URI ...
	 * @param objectVertexId a string id
	 * @param className the class this vertex should be associated with
	 * @return vertex corresponding to that id ... or an exception if none found
	 */
	public Vertex loadVertexFor(String objectVertexId, String className);

	/**
	 * Creates an empty vertex having he given properties
	 * @param valueClass the new value class
	 * @param vertexId new vertex id
	 * @return a new vertex with no value, but a given id (and a relationship with that value class 
	 */
	Vertex createEmptyVertex(Class<? extends Object> valueClass, String vertexId);

	/**
	 * Get id of a gioven vertex. Implementation is free to choose how to find that id
	 * @param objectVertex
	 * @return
	 */
	public String getIdOf(Vertex objectVertex);

	/**
	 * Get effective type of given vertex
	 * @param vertex key for which we want a vertex
	 * @return type of data contained by this vertex. An exception should be thrown when vertex can't provide that info.
	 */
	public String getEffectiveType(Vertex vertex);

	/**
	 * Set value of give node to be the given ... value
	 * @param vertex
	 * @param value
	 */
	public void setValue(Vertex vertex, Object value);

	/**
	 * Get value of given node
	 * @param key
	 * @return
	 */
	public Object getValue(Vertex key);
	
	/**
	 * Grant access to service repository
	 * @return
	 */
	public ServiceRepository getRepository();

	public Edge addEdgeFor(Vertex fromVertex, Vertex toVertex, Property property);
}
