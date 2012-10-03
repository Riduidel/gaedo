package com.dooapp.gaedo.blueprints;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.extensions.id.IdGenerator;
import com.dooapp.gaedo.extensions.id.IntegerGenerator;
import com.dooapp.gaedo.extensions.id.LongGenerator;
import com.dooapp.gaedo.extensions.id.StringGenerator;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.extensions.migrable.VersionMigratorFactory;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.AbstractFinderService;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.PropertyProviderUtils;
import com.dooapp.gaedo.properties.TypeProperty;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Base class for all finder service using blueprints graphs as storage
 * 
 * @author ndx
 * 
 * @param <GraphClass>
 * @param <DataType>
 * @param <InformerType>
 */
public abstract class AbstractBluePrintsBackedFinderService<GraphClass extends Graph, DataType, InformerType extends Informer<DataType>> extends
				AbstractFinderService<DataType, InformerType> implements FinderCrudService<DataType, InformerType>, IdBasedService<DataType> {

	private class DelegatingDriver implements GraphDatabaseDriver {
		@Override
		public Vertex loadVertexFor(String objectVertexId) {
			return AbstractBluePrintsBackedFinderService.this.loadVertexFor(objectVertexId);
		}

		@Override
		public Vertex createEmptyVertex(Class<? extends Object> valueClass, String vertexId) {
			return AbstractBluePrintsBackedFinderService.this.createEmptyVertex(vertexId, valueClass);
		}

		@Override
		public String getIdOf(Vertex objectVertex) {
			return getIdOfVertex(objectVertex);
		}

		@Override
		public String getEffectiveType(Vertex vertex) {
			return AbstractBluePrintsBackedFinderService.this.getEffectiveType(vertex);
		}

		@Override
		public void setValue(Vertex vertex, Object value) {
			AbstractBluePrintsBackedFinderService.this.setValue(vertex, value);
		}

		@Override
		public Object getValue(Vertex vertex) {
			return AbstractBluePrintsBackedFinderService.this.getValue(vertex);
		}

		@Override
		public ServiceRepository getRepository() {
			return AbstractBluePrintsBackedFinderService.this.getRepository();
		}

	}

	private static final Logger logger = Logger.getLogger(IndexableGraphBackedFinderService.class.getName());
	/**
	 * Graph used as database
	 */
	protected final GraphClass database;
	/**
	 * Graph casted as transactional one if possible. It is used to offer
	 * support of transactionnal read operations (if graph is indeed a
	 * transactional one). This field may be NULL.
	 */
	protected final TransactionalGraph transactionSupport;
	/**
	 * Property used to store id
	 */
	private Property idProperty;
	/**
	 * Accelerator cache linking classes objects to the collection of properties
	 * and cascade informations associated to persist those fields.
	 */
	protected Map<Class<?>, Map<Property, Collection<CascadeType>>> classes = new HashMap<Class<?>, Map<Property, Collection<CascadeType>>>();
	/**
	 * Property provider indicating what, and how, saving infos from object
	 */
	protected PropertyProvider propertyProvider;
	/**
	 * Migrator for given contained class
	 */
	protected Migrator migrator;
	/**
	 * Get access to the service repository to handle links between objects
	 */
	protected final ServiceRepository repository;
	/**
	 * Adaptation layer
	 */
	protected BluePrintsPersister persister;
	private boolean requiresIdGeneration;

	public AbstractBluePrintsBackedFinderService(GraphClass graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider) {
		super(containedClass, informerClass, factory);
		this.repository = repository;
		this.propertyProvider = provider;
		this.database = graph;
		if (graph instanceof TransactionalGraph) {
			transactionSupport = (TransactionalGraph) graph;
		} else {
			transactionSupport = null;
		}
		this.idProperty = AnnotationUtils.locateIdField(provider, containedClass, Long.TYPE, Long.class, String.class);
		this.requiresIdGeneration = idProperty.getAnnotation(GeneratedValue.class) != null;
		this.migrator = VersionMigratorFactory.create(containedClass);
		// Updater builds managed nodes here
		this.persister = new BluePrintsPersister(Kind.uri);
		// if there is a migrator, generate property from it
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "created graph service handling " + containedClass.getCanonicalName() + "\n" + "using as id " + idProperty + "\n"
							+ "supporting migration ? " + (migrator != null) + "\n");
		}
	}

	protected abstract Object getValue(Vertex vertex);

	protected abstract void setValue(Vertex vertex, Object value);

	protected abstract String getEffectiveType(Vertex vertex);

	/**
	 * Get id of a given vertex, using any meanys required by implementation
	 * @param objectVertex
	 * @return
	 */
	protected abstract String getIdOfVertex(Vertex objectVertex);
	
	/**
	 * Creates an empty vertex with given vertex id and vertex contained value class
	 * @param vertexId vertex id
	 * @param valueClass value class
	 * @return a vertex storing those informations
	 */
	protected abstract Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass);

	/**
	 * Get map linking properties to their respective cascading informations
	 * 
	 * @param provider
	 *            used provider
	 * @param searchedClass
	 *            searched class
	 * @return a map linking each property to all its cascading informations
	 */
	public Map<Property, Collection<CascadeType>> getPropertiesFor(PropertyProvider provider, Class<?> searchedClass) {
		Map<Property, Collection<CascadeType>> returned = new HashMap<Property, Collection<CascadeType>>();
		Property[] properties = PropertyProviderUtils.getAllProperties(provider, searchedClass);
		for (Property p : properties) {
			if (p.getAnnotation(OneToOne.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(OneToOne.class).cascade()));
			} else if (p.getAnnotation(OneToMany.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(OneToMany.class).cascade()));
			} else if (p.getAnnotation(ManyToMany.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(ManyToMany.class).cascade()));
			} else if (p.getAnnotation(ManyToOne.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(ManyToOne.class).cascade()));
			} else {
				returned.put(p, new LinkedList<CascadeType>());
			}
		}
		// And, if class is the contained one, add the (potential) Migrator
		// property
		if (this.migrator != null) {
			// Migrator has no cascade to be done on
			returned.put(migrator.getMigratorProperty(returned.keySet()), new LinkedList<CascadeType>());
		}
		// Finally, create a fake "classesCollection" property and add it to
		// property
		try {
			returned.put(new ClassCollectionProperty(containedClass), new LinkedList<CascadeType>());
			returned.put(new TypeProperty(containedClass), new LinkedList<CascadeType>());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "what ? a class without a \"class\" field ? WTF", e);
		}
		return returned;
	}

	/**
	 * To put object in graph, we have to find all its fields, then put them in
	 * graph elements. Notice this method directly calls
	 * {@link #doUpdate(Object, CascadeType, Map)}, just checking before that if
	 * an id must be generated. If an id must be generated, then it is (and so
	 * is associated vertex, to make sure no problem will arise later).
	 * 
	 * @param toCreate
	 * @return
	 * @see com.dooapp.gaedo.AbstractCrudService#create(java.lang.Object)
	 */
	@Override
	public DataType create(final DataType toCreate) {
		return new TransactionalOperation<DataType, DataType, InformerType>(this) {

			@Override
			protected DataType doPerform() {
				return doUpdate(toCreate, CascadeType.PERSIST, new TreeMap<String, Object>());
			}
		}.perform();
	}

	private void generateIdFor(DataType toCreate) {
		IdGenerator generator = null;
		Class<?> objectType = Utils.maybeObjectify(idProperty.getType());
		if (Long.class.isAssignableFrom(objectType)) {
			generator = new LongGenerator(this, idProperty);
		} else if (Integer.class.isAssignableFrom(objectType)) {
			generator = new IntegerGenerator(this, idProperty);
		} else if (String.class.isAssignableFrom(objectType)) {
			generator = new StringGenerator(this, idProperty);
		} else {
			throw new UnsupportedIdTypeException(objectType + " can't be used as id : we don't know how to generate its values !");
		}
		generator.generateIdFor(toCreate);
	}

	/**
	 * Delete id and all edges
	 * 
	 * @param toDelete
	 * @see com.dooapp.gaedo.AbstractCrudService#delete(java.lang.Object)
	 */
	@Override
	public void delete(final DataType toDelete) {
		if (toDelete != null) {
			new TransactionalOperation<Void, DataType, InformerType>(this) {

				@Override
				protected Void doPerform() {
					doDelete(toDelete, new TreeMap<String, Object>());
					return null;
				}
			}.perform();
		}
	}

	/**
	 * Local delete implementation
	 * 
	 * @param toDelete
	 */
	private void doDelete(DataType toDelete, Map<String, Object> objectsBeingAccessed) {
		String vertexId = getIdVertexId(toDelete, idProperty, false /*
																	 * no id
																	 * generation
																	 * on delete
																	 */);
		Vertex objectVertex = loadVertexFor(vertexId);
		if (objectVertex != null) {
			Map<Property, Collection<CascadeType>> containedProperties = getContainedProperties(toDelete);
			persister.performDelete(this, vertexId, objectVertex, containedClass, containedProperties, toDelete, CascadeType.REMOVE, objectsBeingAccessed);
		}
	}

	/**
	 * Delete an out edge vertex. Those are vertex corresponding to properties.
	 * 
	 * @param objectVertex
	 *            source object vertex, used for debugging purpose only
	 * @param valueVertex
	 *            value vertex to remove
	 * @param value
	 *            object value
	 */
	<Type> void deleteOutEdgeVertex(Vertex objectVertex, Vertex valueVertex, Type value, Map<String, Object> objectsBeingUpdated) {
		// Locate vertex
		Vertex knownValueVertex = getVertexFor(value, CascadeType.REFRESH, objectsBeingUpdated);
		// Ensure vertex is our out one
		if (valueVertex.equals(knownValueVertex)) {
			// Delete vertex and other associated ones, only if they have no
			// other input links (elsewhere delete is silently ignored)
			if (valueVertex.getInEdges().iterator().hasNext()) {
				// There are incoming edges to that vertex. Do nothing but log
				// it
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE,
									"while deleting " + GraphUtils.toString(objectVertex) + "" + " we tried to delete " + GraphUtils.toString(knownValueVertex)
													+ "" + " which has other incoming edges, so we didn't deleted it");
				}
			} else {
				// OK, time to delete value vertex. Is it a managed node ?
				if (repository.containsKey(value.getClass())) {
					FinderCrudService<Type, ?> finderCrudService = (FinderCrudService<Type, ?>) repository.get(value.getClass());
					finderCrudService.delete(value);
				} else {
					// Literal nodes can be deleted without any trouble
					database.removeVertex(valueVertex);
				}
			}
		} else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "that's strange : value " + value + " is associated to " + GraphUtils.toString(knownValueVertex) + ""
								+ " which blueprints says is different from " + GraphUtils.toString(valueVertex) + "."
								+ " Under those circumstances, we can delete neither of them");
			}
		}
	}

	public Map<Property, Collection<CascadeType>> getContainedProperties(DataType object) {
		Class<? extends Object> objectClass = object.getClass();
		return getContainedProperties(objectClass);
	}

	public Map<Property, Collection<CascadeType>> getContainedProperties(Class<? extends Object> objectClass) {
		if (!classes.containsKey(objectClass)) {
			classes.put(objectClass, getPropertiesFor(propertyProvider, objectClass));
		}
		return classes.get(objectClass);
	}

	/**
	 * Gets the id vertex for the given object (if that object exists)
	 * 
	 * @param object
	 *            object to get id vertex for
	 * @param allowIdGeneration
	 *            when set to true, an id may be created for that object
	 * @return first matching node if found, and null if not
	 */
	private Vertex getIdVertexFor(DataType object, boolean allowIdGeneration) {
		return loadVertexFor(getIdVertexId(object, idProperty, allowIdGeneration));
	}

	/**
	 * Notice it only works if id is a literal type
	 * 
	 * @param object
	 *            object for which we want the id vertex id property
	 * @param idProperty
	 *            property used to extract id from object
	 * @param requiresIdGeneration
	 *            set to true when effective id generation is required. Allow to
	 *            generate id only on create operations
	 * @return a composite id containing the service class, the data class and
	 *         the the instance value
	 * @see GraphUtils#getIdVertexId(IndexableGraph, Class, Object, Property)
	 */
	private String getIdVertexId(DataType object, Property idProperty, boolean requiresIdGeneration) {
		if (requiresIdGeneration) {
			// Check value of idProperty
			Object value = idProperty.get(object);
			if (value == null) {
				generateIdFor(object);
			} else if (Number.class.isAssignableFrom(Utils.maybeObjectify(idProperty.getType()))) {
				Number n = (Number) value;
				if (n.equals(0) || n.equals(0l)) {
					generateIdFor(object);
				}
			}
		}
		return GraphUtils.getIdVertexId(database, object.getClass(), object, idProperty);
	}

	/**
	 * Get id of given object, provided of course it's an instance of this class
	 * 
	 * @param data
	 *            object to extract an id for
	 * @return id of that object
	 */
	public Object getIdOf(DataType data) {
		return getIdVertexId(data, idProperty, false);
	}

	@Override
	public DataType update(final DataType toUpdate) {
		return new TransactionalOperation<DataType, DataType, InformerType>(this) {

			@Override
			protected DataType doPerform() {
				return doUpdate(toUpdate, CascadeType.MERGE, new TreeMap<String, Object>());
			}
		}.perform();
	}

	/**
	 * here is a trick : we want id generation to happen only on first persist
	 * (that's to say on call to #create), but not on subsequent ones. So, as
	 * first call uses CascadeType.PERSIST and others uses CascadeType.MERGE, we
	 * can use that indication to separate them. It has the unfortunate
	 * inconvenient to force us to use only PERSIST during #create
	 * 
	 * @param toUpdate
	 *            object to update
	 * @param cascade
	 *            type. As mentionned upper, beware to value used !
	 * @param treeMap
	 *            map of objects already used
	 */
	private DataType doUpdate(DataType toUpdate, CascadeType cascade, Map<String, Object> treeMap) {
		boolean generatesId = requiresIdGeneration ? (CascadeType.PERSIST == cascade) : false;
		String objectVertexId = getIdVertexId(toUpdate, idProperty, generatesId);
		Vertex objectVertex = loadVertexFor(objectVertexId);
		return (DataType) persister.performUpdate(this, objectVertexId, objectVertex, toUpdate.getClass(), getContainedProperties(toUpdate), toUpdate, cascade,
						treeMap);
	}

	/**
	 * Get vertex associated to value. If object is managed by a service, we ask
	 * this service the value
	 * 
	 * @param value
	 *            value we want the vertex for
	 * @param cascade
	 *            used cascade type, can be either {@link CascadeType#PERSIST}
	 *            or {@link CascadeType#MERGE}
	 * @param objectsBeingUpdated
	 *            map of objects currently being updated, it avoid some loops
	 *            during update, but is absolutely NOT a persistent cache
	 * @return
	 */
	public Vertex getVertexFor(Object value, CascadeType cascade, Map<String, Object> objectsBeingUpdated) {
		boolean allowIdGeneration = CascadeType.PERSIST.equals(cascade) || CascadeType.MERGE.equals(cascade);
		// Here we suppose the service is the right one for the job (which may
		// not be the case)
		if (containedClass.isInstance(value)) {
			Vertex returned = getIdVertexFor(containedClass.cast(value), allowIdGeneration);
			if (returned == null) {
				doUpdate(containedClass.cast(value), cascade, objectsBeingUpdated);
				returned = getIdVertexFor(containedClass.cast(value), allowIdGeneration);
			} else {
				// vertex already exist, but maybe object needs an update
				if (CascadeType.PERSIST == cascade || CascadeType.MERGE == cascade) {
					doUpdate(containedClass.cast(value), cascade, objectsBeingUpdated);
				}
			}
			return returned;
		}
		Class<? extends Object> valueClass = value.getClass();
		if (repository.containsKey(valueClass)) {
			FinderCrudService service = repository.get(valueClass);
			if (service instanceof IndexableGraphBackedFinderService) {
				return ((IndexableGraphBackedFinderService) service).getVertexFor(value, cascade, objectsBeingUpdated);
			} else {
				throw new IncompatibleServiceException(service, valueClass);
			}
		} else if (Literals.containsKey(valueClass)) {
			return GraphUtils.getVertexForLiteral(getDriver(), value);
		} else if (Tuples.containsKey(valueClass)) {
			return GraphUtils.getVertexForTuple(this, repository, value, objectsBeingUpdated);
		} else {
			/*
			 * // OK, we will persist this object by ourselves, which is really
			 * error-prone, but we do we have any other solution ? // But notice
			 * object is by design consderie Vertex objectVertex =
			 * objectVertex.setProperty(Properties.vertexId.name(),
			 * getIdVertexId(toUpdate));
			 * objectVertex.setProperty(Properties.kind.name(),
			 * Kind.managed.name());
			 * objectVertex.setProperty(Properties.type.name(),
			 * toUpdate.getClass().getName());
			 */
			throw new ObjectIsNotARealLiteralException(value, valueClass);

		}
	}

	/**
	 * Object query is done by simply looking up all objects of that class using
	 * a standard query
	 * 
	 * @return an iterable over all objects of that class
	 * @see com.dooapp.gaedo.finders.FinderCrudService#findAll()
	 */
	@Override
	public Iterable<DataType> findAll() {
		return find().matching(new QueryBuilder<InformerType>() {

			/**
			 * An empty and starts with an initial match of true, but degrades
			 * it for each failure. So creating an empty and() is like creating
			 * a "true" statement, which in turn results into searching all
			 * objects of that class.
			 * 
			 * @param informer
			 * @return an empty or matching all objects
			 * @see com.dooapp.gaedo.finders.QueryBuilder#createMatchingExpression(com.dooapp.gaedo.finders.Informer)
			 */
			@Override
			public QueryExpression createMatchingExpression(InformerType informer) {
				return Expressions.and();
			}
		}).getAll();
	}

	/**
	 * Load object starting with the given vertex root. Notice object is added
	 * to the accessed set with a weak key, this way, it should be faster to
	 * load it and to maintain instance unicity
	 * 
	 * @param objectVertex
	 * 
	 * @return loaded object
	 * @param objectsBeingAccessed
	 *            map of objects currently being accessed, it avoid some loops
	 *            during loading, but is absolutely NOT a persistent cache
	 * @see #loadObject(String, Vertex, Map)
	 */
	public DataType loadObject(String objectVertexId, Map<String, Object> objectsBeingAccessed) {
		// If cast fails, well, that's some fuckin mess, no ?
		Vertex objectVertex = loadVertexFor(objectVertexId);
		return persister.loadObject(this, objectVertexId, objectVertex, objectsBeingAccessed);
	}

	/**
	 * Load veretx associated to given object id.
	 * 
	 * @param objectVertexId
	 *            vertex id for which we want a vertex
	 * @return loaded vertex if found, or an exception (I guess ?) if none found
	 */
	public abstract Vertex loadVertexFor(String objectVertexId);

	/**
	 * Load object from a vertex
	 * 
	 * @param objectVertex
	 * @param objectsBeingAccessed
	 *            map of objects currently being accessed, it avoid some loops
	 *            during loading, but is absolutely NOT a persistent cache
	 * @return loaded object
	 * @see #loadObject(String, Vertex, Map)
	 */
	public DataType loadObject(Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		return persister.loadObject(this, objectVertex, objectsBeingAccessed);
	}

	/**
	 * we only consider first id element
	 * 
	 * @param id
	 *            collection of id
	 * @return object which has as vertexId the given property
	 * @see com.dooapp.gaedo.finders.id.IdBasedService#findById(java.lang.Object[])
	 */
	@Override
	public DataType findById(final Object... id) {
		// make sure entered type is a valid one
		if (Utils.maybeObjectify(idProperty.getType()).isAssignableFrom(Utils.maybeObjectify(id[0].getClass()))) {
			String vertexIdValue = GraphUtils.getIdOfLiteral(containedClass, idProperty, id[0]).toString();
			Vertex rootVertex = loadVertexFor(vertexIdValue);
			if (rootVertex == null) {
				try {
					// root vertex couldn't be found directly, mostly due to
					// https://github.com/Riduidel/gaedo/issues/11
					// So perform the longer (but always working) query
					return find().matching(new QueryBuilder<InformerType>() {

						@Override
						public QueryExpression createMatchingExpression(InformerType informer) {
							return informer.get(idProperty.getName()).equalsTo(id[0]);
						}
					}).getFirst();
				} catch (NoReturnableVertexException e) {
					// due to getFirst semantics, an exception has to be thrown
					// when no entry is found, which is off lesser interest
					// here, that why we catch it to return null instead
					return null;
				}
			} else {
				// root vertex can be directly found ! so load it immediatly
				return loadObject(vertexIdValue, new TreeMap<String, Object>());
			}
		} else {
			throw new UnsupportedIdException(id[0].getClass(), idProperty.getType());
		}
	}

	@Override
	public Collection<Property> getIdProperties() {
		return Arrays.asList(idProperty);
	}

	/**
	 * Get object associated to given key. Notice this method uses internal
	 * cache ({@link #objectsBeingAccessed}) before to resolve call on
	 * datastore.
	 * 
	 * @param key
	 * @return
	 */
	public DataType getObjectFromKey(String key) {
		return loadObject(key, new TreeMap<String, Object>());
	}

	/**
	 * @return the database
	 * @category getter
	 * @category database
	 */
	public GraphClass getDatabase() {
		return database;
	}

	/**
	 * @return the repository
	 * @category getter
	 * @category repository
	 */
	public ServiceRepository getRepository() {
		return repository;
	}

	/**
	 * Set id of object, and try to assign that object a vertex.
	 * 
	 * @param value
	 * @param id
	 * @return
	 * @see com.dooapp.gaedo.finders.id.IdBasedService#assignId(java.lang.Object,
	 *      java.lang.Object[])
	 */
	@Override
	public boolean assignId(final DataType value, Object... id) {
		/*
		 * We first make sure object is an instance of containedClass. This way,
		 * we can then use value class to create id vertex
		 */
		if (containedClass.isInstance(value)) {
			idProperty.set(value, id[0]);
			if (getIdVertexFor(value, false /*
											 * no id generation when assigning
											 * an id !
											 */) == null) {
				try {
					TransactionalOperation<Boolean, DataType, InformerType> operation = new TransactionalOperation<Boolean, DataType, InformerType>(this) {

						@Override
						protected Boolean doPerform() {
							String idVertexId = getIdVertexId(value, idProperty, requiresIdGeneration);
							Vertex returned = getDriver().createEmptyVertex(value.getClass(), idVertexId);
							getDriver().setValue(returned, idVertexId);
							return true;
						}
					};
					return operation.perform();
				} catch (Exception e) {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @param requiresIdGeneration
	 *            the requiresIdGeneration to set
	 * @category setter
	 * @category requiresIdGeneration
	 */
	void setRequiresIdGeneration(boolean requiresIdGeneration) {
		this.requiresIdGeneration = requiresIdGeneration;
	}

	/**
	 * @return the requiresIdGeneration
	 * @category getter
	 * @category requiresIdGeneration
	 */
	public boolean isRequiresIdGeneration() {
		return requiresIdGeneration;
	}

	/**
	 * Provides driver view to database. This driver is a way for us to expose
	 * low-level infos to graph without breaking huigh-level abstraction of a
	 * FinderService.
	 * 
	 * @return
	 */
	public GraphDatabaseDriver getDriver() {
		return new DelegatingDriver();
	}

}
