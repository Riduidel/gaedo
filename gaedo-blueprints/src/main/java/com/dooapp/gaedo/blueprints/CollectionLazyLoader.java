package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class CollectionLazyLoader extends AbstractLazyLoader implements InvocationHandler, WriteReplaceable, Serializable {

	// Internal storage collection (not to be confused with external visible collection)
	private Collection collection;
	/**
	 * Serialization constructor
	 */
	public CollectionLazyLoader() {
		
	}

	public CollectionLazyLoader(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, ServiceRepository repository, Property p, Vertex objectVertex, Collection<Object> targetCollection, Map<String, Object> objectsBeingAccessed) {
		super(driver, strategy, p, objectVertex, repository, classLoader, objectsBeingAccessed);
		this.collection = targetCollection;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(!loaded) {
			loadCollection(collection, objectsBeingAccessed);
		}
		if(method.getDeclaringClass().equals(WriteReplaceable.class)) {
			// there is only writeReplace there, so writeReplace !
			return collection;
		}
		return method.invoke(collection, args);
	}

	public void loadCollection(Collection collection, Map<String, Object> objectsBeingAccessed) {
		try {
			for(Edge e : rootVertex.getOutEdges(edgeName)) {
				Vertex value = e.getInVertex();
				Object temporaryValue = GraphUtils.createInstance(driver, strategy, classLoader, value, property.getType(), repository, objectsBeingAccessed);
				if(repository.containsKey(temporaryValue.getClass())) {
					FinderCrudService service = repository.get(temporaryValue.getClass());
					if (service instanceof AbstractBluePrintsBackedFinderService) {
						AbstractBluePrintsBackedFinderService<?, ?, ?> blueprints= (AbstractBluePrintsBackedFinderService<?, ?, ?>) service;
						collection.add(blueprints.loadObject(value, objectsBeingAccessed));
					}
				} else {
					// Instance should be OK, as createinstance should support everything getVertexForBasicObject supports
					collection.add(temporaryValue);
				}
			}
		} finally {
			loaded = true;
		}
	}

	@Override
	public Object writeReplace() throws ObjectStreamException {
		if(!loaded) {
			loadCollection(collection, objectsBeingAccessed);
		}
		return collection;
	}
}
