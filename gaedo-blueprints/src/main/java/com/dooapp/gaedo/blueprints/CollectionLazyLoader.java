package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.operations.CollectionAccessByIndexProperty;
import com.dooapp.gaedo.blueprints.operations.CollectionSizeProperty;
import com.dooapp.gaedo.blueprints.operations.LiteralInCollectionUpdaterProperty;
import com.dooapp.gaedo.blueprints.operations.Loader;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

@SuppressWarnings("rawtypes")
public class CollectionLazyLoader extends AbstractLazyLoader implements InvocationHandler, WriteReplaceable, Serializable {
	/**
	 * As values can be laoded from both edges and node properties, these different methods have to be implemented in different fashions.
	 * That's why I've created this lcoal interface, that will allow us to load both opf them in the order
	 * defined by collection_index while respecting the way they're stored.
	 * @author ndx
	 *
	 */
	private interface ValueLoader {

		Object load(ObjectCache objectsBeingAccessed);

	}

	public class LoadValueInProperty implements ValueLoader {

		private Property toLoad;

		public LoadValueInProperty(AbstractPropertyAdapter elementByIndexProperty) {
			this.toLoad = elementByIndexProperty;
		}

		@Override
		public Object load(ObjectCache objectsBeingAccessed) {
			return Loader.loadSingleLiteral(classLoader, toLoad, rootVertex, objectsBeingAccessed);
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LoadValueInProperty [");
			if (toLoad != null) {
				builder.append("toLoad=");
				builder.append(toLoad);
			}
			if (rootVertex != null) {
				builder.append("rootVertex=");
				builder.append(GraphUtils.toString(rootVertex));
			}
			builder.append("]");
			return builder.toString();
		}

	}

	public class LoadValueBehindEdge implements ValueLoader {

		private Edge toLoad;

		public LoadValueBehindEdge(Edge e) {
			this.toLoad = e;
		}

		@Override
		public Object load(ObjectCache objectsBeingAccessed) {
			Vertex value = toLoad.getVertex(Direction.IN);
			return loadValue(objectsBeingAccessed, value);
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LoadValueBehindEdge [");
			if (toLoad != null) {
				builder.append("toLoad=");
				builder.append(GraphUtils.toString(toLoad));
			}
			builder.append("]");
			return builder.toString();
		}

	}

	private static final Logger logger = Logger.getLogger(CollectionLazyLoader.class.getName());

	// Internal storage collection (not to be confused with external visible collection)
	private Collection collection;

	/**
	 * Serialization constructor
	 */
	public CollectionLazyLoader() {
	}

	public CollectionLazyLoader(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, ServiceRepository repository, Property p, Vertex objectVertex, Collection<Object> targetCollection, ObjectCache objectsBeingAccessed) {
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
	public void loadCollection(Collection collection, ObjectCache objectsBeingAccessed) {
		try {
			// First thing first, get collection size, which is stored during update
			CollectionSizeProperty sizeProperty = new CollectionSizeProperty(property);
			Integer collectionSize = (Integer) Loader.loadSingleLiteral(classLoader, sizeProperty, rootVertex, objectsBeingAccessed);
			// using a treemap to allow filling in any order AND unnumbered items to be at the end
			Map<Integer, ValueLoader> loaders = new TreeMap<Integer, CollectionLazyLoader.ValueLoader>();
			loaders.putAll(addEdgesLoaders());
			loaders.putAll(addPropertyValueLoaders());

			for(ValueLoader loader : loaders.values()) {
				try {
					collection.add(loader.load(objectsBeingAccessed));
				} catch(UnableToCreateException ex) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "we failed to load value associated with loader "+loader.toString(), ex);
					}
				}
			}
		} finally {
			loaded = true;
		}
	}

	private Map<? extends Integer, ? extends ValueLoader> addPropertyValueLoaders() {
		Map<Integer, ValueLoader> returned = new TreeMap<Integer, ValueLoader>();
		String propertyNamePrefix = GraphUtils.getEdgeNameFor(property);
		for(String propertyName : rootVertex.getPropertyKeys()) {
			Map.Entry<String, String> mapping = LiteralInCollectionUpdaterProperty.getKey(propertyName, property);
			if(mapping!=null && mapping.getKey().equals(propertyNamePrefix)) {
				// a valid property for that map ! is property key an integer ?
				try {
					int index = Integer.parseInt(mapping.getValue());
					returned.put(index, new LoadValueInProperty(new CollectionAccessByIndexProperty(property, index, null)));
				} catch(NumberFormatException e) {
					// nothing to do : value is just non matching.
				}
			}
		}
		return returned;
	}

	protected Map<Integer, ValueLoader> addEdgesLoaders() {
		Map<Integer, ValueLoader> returned = new TreeMap<Integer, ValueLoader>();
		for(Edge e : strategy.getOutEdgesFor(rootVertex, property)) {
			if(e.getProperty(Properties.collection_index.name()) != null) {
				returned.put((Integer) e.getProperty(Properties.collection_index.name()), new LoadValueBehindEdge(e));
			} else {
				// These items are pushed to the latest element (after collectionSize) which has no value set
				int index = Integer.MAX_VALUE-1;
				while(returned.containsKey(index)) { index--; }
				returned.put(index, new LoadValueBehindEdge(e));
			}
		}
		return returned;
	}

	protected Object loadValue(ObjectCache objectsBeingAccessed, Vertex value) {
		Object temporaryValue = GraphUtils.createInstance(driver, strategy, classLoader, value, property.getType(), repository, objectsBeingAccessed);
		if(repository.containsKey(temporaryValue.getClass())) {
			FinderCrudService service = repository.get(temporaryValue.getClass());
			if (service instanceof AbstractBluePrintsBackedFinderService) {
				AbstractBluePrintsBackedFinderService<?, ?, ?> blueprints= (AbstractBluePrintsBackedFinderService<?, ?, ?>) service;
				temporaryValue = blueprints.loadObject(value, objectsBeingAccessed);
			}
		} else {
			// Instance should be OK, as createinstance should support everything getVertexForBasicObject supports
		}
		return temporaryValue;
	}

	@Override
	public Object writeReplace() throws ObjectStreamException {
		if(!loaded) {
			loadCollection(collection, objectsBeingAccessed);
		}
		return collection;
	}
}
