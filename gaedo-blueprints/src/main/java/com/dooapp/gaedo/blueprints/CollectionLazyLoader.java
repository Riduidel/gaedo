package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

@SuppressWarnings("rawtypes")
public class CollectionLazyLoader extends AbstractLazyLoader implements InvocationHandler, WriteReplaceable, Serializable {
	private static final Logger logger = Logger.getLogger(CollectionLazyLoader.class.getName());

	// Internal storage collection (not to be confused with external visible collection)
	private Collection collection;
	
	private static Comparator<Edge> COLLECTION_EDGE_COMPARATOR = new Comparator<Edge>() {
		@Override
		public int compare(Edge o1, Edge o2) {
			Integer o1Idx = (Integer) o1.getProperty(Properties.collection_index.name());
			Integer o2Idx = (Integer) o2.getProperty(Properties.collection_index.name());
			
			if(null == o1Idx || null == o2Idx)
				return 0;
			
			return o1Idx.compareTo(o2Idx);
		}
	};
	
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

	@SuppressWarnings("unchecked")
	public void loadCollection(Collection collection, Map<String, Object> objectsBeingAccessed) {
		try {
			// Use the magic order property to try to put the elements in the correct order (if the property is there)
			List<Edge> edges = new LinkedList<Edge>();
			boolean needToSort = false;
			for(Edge e : rootVertex.getOutEdges(edgeName)) {
				edges.add(e);
				if(e.getProperty(Properties.collection_index.name()) != null)
					needToSort = true;
			}
			if(needToSort)
				Collections.sort(edges, COLLECTION_EDGE_COMPARATOR);
			
			// Now that everything is in order, we can load the real collection
			for(Edge e : edges) {
//			for(Edge e : rootVertex.getOutEdges(edgeName)) {
				Vertex value = e.getInVertex();
				try {
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
				} catch(UnableToCreateException ex) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "we failed to load value associated with vertex "+GraphUtils.toString(value), ex);
					}
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
