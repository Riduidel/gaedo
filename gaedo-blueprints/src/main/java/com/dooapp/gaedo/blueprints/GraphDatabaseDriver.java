package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.Graph;
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
	 * @param objectVertexI a string idd
	 * @return vertex corresponding to that id ... or an exception if none found
	 */
	public Vertex loadVertexFor(String objectVertexId);

	/**
	 * Creates an empty vertex having he given properties
	 * @param vertexId new vertex id
	 * @param valueClass the new value class
	 * @return a new vertex with no value, but a given id (and a relationship with that value class 
	 */
	Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass);

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
}
