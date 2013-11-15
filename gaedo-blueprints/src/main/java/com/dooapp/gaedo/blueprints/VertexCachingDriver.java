package com.dooapp.gaedo.blueprints;

import java.util.Map;
import java.util.TreeMap;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * An attempt to improve some delays by maintaining a proximity cache.
 * This cache
 * @author ndx
 *
 */
public class VertexCachingDriver implements GraphDatabaseDriver {

	private Map<String, Vertex> knownVertices = new TreeMap<String, Vertex>();
	private GraphDatabaseDriver delegate;

	public VertexCachingDriver(GraphDatabaseDriver driver) {
		this.delegate = driver;
	}

	private Vertex cache(Vertex objectVertex) {
		if(objectVertex!=null)
			knownVertices.put(valueOf(objectVertex), objectVertex);
		return objectVertex;
	}

	/**
	 * @param objectVertexId
	 * @param className
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#loadVertexFor(java.lang.String, java.lang.String)
	 * @category delegate
	 */
	public Vertex loadVertexFor(String objectVertexId, String className) {
		if(knownVertices.containsKey(objectVertexId)) {
			return knownVertices.get(objectVertexId);
		} else {
			return cache(delegate.loadVertexFor(objectVertexId, className));
		}
	}

	/**
	 * @param valueClass
	 * @param vertexId
	 * @param value
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#createEmptyVertex(java.lang.Class, java.lang.String, java.lang.Object)
	 * @category delegate
	 */
	public Vertex createEmptyVertex(Class<? extends Object> valueClass, String vertexId, Object value) {
		return cache(delegate.createEmptyVertex(valueClass, vertexId, value));
	}

	/**
	 * @param objectVertex
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#getIdOf(com.tinkerpop.blueprints.Vertex)
	 * @category delegate
	 */
	public String getIdOf(Vertex objectVertex) {
		return delegate.getIdOf(objectVertex);
	}

	/**
	 * @param vertex
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#getEffectiveType(com.tinkerpop.blueprints.Vertex)
	 * @category delegate
	 */
	public String getEffectiveType(Vertex vertex) {
		return delegate.getEffectiveType(vertex);
	}

	/**
	 * @param vertex
	 * @param value
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#setValue(com.tinkerpop.blueprints.Vertex, java.lang.Object)
	 * @category delegate
	 */
	public void setValue(Vertex vertex, Object value) {
		knownVertices.remove(valueOf(vertex));
		delegate.setValue(vertex, value);
		cache(vertex);
	}

	private String valueOf(Vertex vertex) {
		return getValue(vertex).toString();
	}

	/**
	 * @param key
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#getValue(com.tinkerpop.blueprints.Vertex)
	 * @category delegate
	 */
	public Object getValue(Vertex key) {
		return delegate.getValue(key);
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#getRepository()
	 * @category delegate
	 */
	public ServiceRepository getRepository() {
		return delegate.getRepository();
	}

	/**
	 * @param fromVertex
	 * @param toVertex
	 * @param property
	 * @return
	 * @see com.dooapp.gaedo.blueprints.GraphDatabaseDriver#createEdgeFor(com.tinkerpop.blueprints.Vertex, com.tinkerpop.blueprints.Vertex, com.dooapp.gaedo.properties.Property)
	 * @category delegate
	 */
	public Edge createEdgeFor(Vertex fromVertex, Vertex toVertex, Property property) {
		return delegate.createEdgeFor(fromVertex, toVertex, property);
	}

	@Override
	public void removeSafely(Vertex notYetDeleted) {
		knownVertices.remove(valueOf(notYetDeleted));
		delegate.removeSafely(notYetDeleted);
	}
}
