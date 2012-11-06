package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.util.Map;

import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public abstract class AbstractLazyLoader {

	protected transient Property property;
	protected transient Vertex rootVertex;
	protected transient ServiceRepository repository;
	protected boolean loaded = false;
	protected ClassLoader classLoader;
	protected Map<String, Object> objectsBeingAccessed;
	/**
	 * Edge name is eagerly determined from known elements to fasten lazy loading (and avoid some service unavailability issues
	 */
	protected String edgeName;
	protected GraphDatabaseDriver driver;
	protected transient GraphMappingStrategy strategy;
	
	public AbstractLazyLoader() {
		
	}

	public AbstractLazyLoader(GraphDatabaseDriver driver, GraphMappingStrategy strategy, Property property, Vertex rootVertex, ServiceRepository repository,
					ClassLoader classLoader, Map<String, Object> objectsBeingAccessed) {
		super();
		this.driver = driver;
		this.strategy = strategy;
		this.property = property;
		this.rootVertex = rootVertex;
		this.repository = repository;
		this.classLoader = classLoader;
		this.objectsBeingAccessed = objectsBeingAccessed;
		this.edgeName = GraphUtils.getEdgeNameFor(property);
	}

	private Object writeReplace() throws ObjectStreamException {
		throw new UnsupportedOperationException("that's the proxy that should call writeReplace !");
	}

}
