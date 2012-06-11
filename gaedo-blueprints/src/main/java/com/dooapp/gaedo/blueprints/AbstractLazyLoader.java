package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.util.Map;

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
	
	public AbstractLazyLoader() {
		
	}

	public AbstractLazyLoader(Property property, Vertex rootVertex, ServiceRepository repository, ClassLoader classLoader,
					Map<String, Object> objectsBeingAccessed) {
		super();
		this.property = property;
		this.rootVertex = rootVertex;
		this.repository = repository;
		this.classLoader = classLoader;
		this.objectsBeingAccessed = objectsBeingAccessed;
	}

	private Object writeReplace() throws ObjectStreamException {
		throw new UnsupportedOperationException("that's the proxy that should call writeReplace !");
	}

}
