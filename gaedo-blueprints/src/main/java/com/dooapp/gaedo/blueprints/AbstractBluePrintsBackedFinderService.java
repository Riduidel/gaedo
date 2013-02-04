package com.dooapp.gaedo.blueprints;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.blueprints.strategies.StrategyUtils;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.extensions.migrable.VersionMigratorFactory;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.AbstractFinderService;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.tinkerpop.blueprints.pgm.Edge;
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
public abstract class AbstractBluePrintsBackedFinderService<GraphClass extends Graph, DataType, InformerType extends Informer<DataType>> 
	extends	AbstractFinderService<DataType, InformerType> 
	implements InViewService<DataType, InformerType, SortedSet<String>>, 
		IdBasedService<DataType> {

	private class DelegatingDriver implements GraphDatabaseDriver {
		@Override
		public Vertex loadVertexFor(String objectVertexId, String className) {
			return AbstractBluePrintsBackedFinderService.this.loadVertexFor(objectVertexId, className);
		}

		@Override
		public Vertex createEmptyVertex(Class<? extends Object> valueClass, String vertexId, Object value) {
			return AbstractBluePrintsBackedFinderService.this.createEmptyVertex(vertexId, valueClass, value);
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

		@Override
		public Edge createEdgeFor(Vertex fromVertex, Vertex toVertex, Property property) {
			return AbstractBluePrintsBackedFinderService.this.createEdgeFor(fromVertex, toVertex, property);
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
	 * Accelerator cache linking classes objects to the collection of properties
	 * and cascade informations associated to persist those fields.
	 */
	protected Map<Class<?>, Map<Property, Collection<CascadeType>>> classes = new HashMap<Class<?>, Map<Property, Collection<CascadeType>>>();
	/**
	 * Property provider indicating what, and how, saving infos from object
	 */
	protected final PropertyProvider propertyProvider;
	/**
	 * Migrator for given contained class
	 */
	protected final Migrator migrator;
	/**
	 * Get access to the service repository to handle links between objects
	 */
	protected final ServiceRepository repository;
	/**
	 * Adaptation layer
	 */
	protected BluePrintsPersister persister;
	private GraphMappingStrategy<DataType> strategy;

	/**
	 * Used lens is a list of named graphs uris
	 */
	protected SortedSet<String> lens = new TreeSet<String>(Arrays.asList(GraphUtils.GAEDO_CONTEXT));

	public AbstractBluePrintsBackedFinderService(GraphClass graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider) {
		this(graph, containedClass, informerClass, factory, repository, provider, StrategyType.beanBased);
	}

	/**
	 * Constructor defining a service using a strategy type
	 * @param graph
	 * @param containedClass
	 * @param informerClass
	 * @param factory
	 * @param repository2
	 * @param provider
	 * @param beanbased
	 */
	public AbstractBluePrintsBackedFinderService(GraphClass graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, StrategyType strategy) {
		this(graph, containedClass, informerClass, factory, repository, provider, 
						StrategyUtils.loadStrategyFor(strategy, containedClass, provider, VersionMigratorFactory.create(containedClass)));
	}

	public AbstractBluePrintsBackedFinderService(GraphClass graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory informerFactory,
					ServiceRepository repository, PropertyProvider provider, GraphMappingStrategy<DataType> strategy) {
		super(containedClass, informerClass, informerFactory);
		this.repository = repository;
		this.propertyProvider = provider;
		this.database = graph;
		if (graph instanceof TransactionalGraph) {
			transactionSupport = (TransactionalGraph) graph;
		} else {
			transactionSupport = null;
		}
		this.strategy = strategy;
		strategy.reloadWith(this);
		this.migrator = VersionMigratorFactory.create(containedClass);
		// Updater builds managed nodes here
		this.persister = new BluePrintsPersister(Kind.uri);
		// if there is a migrator, generate property from it
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "created graph service handling " + containedClass.getCanonicalName());
		}
	}
	
	@Override
	public InformerFactory getInformerFactory() {
		return super.getInformerFactory();
	}

	protected abstract Edge createEdgeFor(Vertex fromVertex, Vertex toVertex, Property property);

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
	 * @param value TODO
	 * @return a vertex storing those informations
	 */
	protected abstract Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass, Object value);

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
		String vertexId = getIdVertexId(toDelete, false /*
																	 * no id
																	 * generation
																	 * on delete
																	 */);
		Class<? extends Object> toDeleteClass = toDelete.getClass();
		Vertex objectVertex = loadVertexFor(vertexId, toDeleteClass.getName());
		if (objectVertex != null) {
			Map<Property, Collection<CascadeType>> containedProperties = strategy.getContainedProperties(toDelete, objectVertex, CascadeType.REMOVE);
			persister.performDelete(this, vertexId, objectVertex, toDeleteClass, containedProperties, toDelete, CascadeType.REMOVE, objectsBeingAccessed);
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
		// Delete vertex and other associated ones, only if they have no
		// other input links (elsewhere delete is silently ignored)
		if (valueVertex.getInEdges().iterator().hasNext()) {
			// There are incoming edges to that vertex. Do nothing but log
			// it
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE,
								"while deleting " + GraphUtils.toString(objectVertex) + "" + " we tried to delete " + GraphUtils.toString(valueVertex)
												+ "" + " which has other incoming edges, so we didn't deleted it");
			}
		} else {
			// OK, time to delete value vertex. Is it a managed node ?
			if (repository.containsKey(value.getClass())) {
				FinderCrudService<Type, ?> finderCrudService = (FinderCrudService<Type, ?>) repository.get(value.getClass());
				if (finderCrudService instanceof AbstractBluePrintsBackedFinderService) {
					((AbstractBluePrintsBackedFinderService<?, Type, ?>) finderCrudService).doDelete(value, objectsBeingUpdated);
				} else {
					throw new IncompatibleServiceException(finderCrudService, value.getClass());
				}
			} else {
				// Literal nodes can be deleted without any trouble
				GraphUtils.removeSafely(database, valueVertex);
			}
		}
	}

	/**
	 * Gets the id vertex for the given object (if that object exists).
 	 * Method is made public to allow some tests to call it
	 * 
	 * @param object
	 *            object to get id vertex for
	 * @param allowIdGeneration
	 *            when set to true, an id may be created for that object
	 * @return first matching node if found, and null if not
	 */
	public Vertex getIdVertexFor(DataType object, boolean allowIdGeneration) {
		return loadVertexFor(getIdVertexId(object, allowIdGeneration), object.getClass().getName());
	}

	/**
	 * Notice it only works if id is a literal type.
	 * Method is made public to allow some tests to call it
	 * 
	 * @param object
	 *            object for which we want the id vertex id property
	 * @param requiresIdGeneration
	 *            set to true when effective id generation is required. Allow to
	 *            generate id only on create operations
	 * @return a composite id containing the service class, the data class and
	 *         the the instance value
	 * @see GraphUtils#getIdVertexId(IndexableGraph, Class, Object, Property)
	 */
	String getIdVertexId(DataType object, boolean requiresIdGeneration) {
		if (requiresIdGeneration) {
			strategy.generateValidIdFor(object);
		}
		return strategy.getIdString(object);
	}

	/**
	 * Get id of given object, provided of course it's an instance of this class
	 * 
	 * @param data
	 *            object to extract an id for
	 * @return id of that object
	 */
	public Object getIdOf(DataType data) {
		return getIdVertexId(data, false);
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
		boolean generatesId = strategy.isIdGenerationRequired() ? (CascadeType.PERSIST == cascade) : false;
		String objectVertexId = getIdVertexId(toUpdate, generatesId);
		Class<? extends Object> toUpdateClass = toUpdate.getClass();
		Vertex objectVertex = loadVertexFor(objectVertexId, toUpdateClass.getName());
		Map<Property, Collection<CascadeType>> containedProperties = strategy.getContainedProperties(toUpdate, objectVertex, cascade);
		return (DataType) persister.performUpdate(this, objectVertexId, objectVertex, toUpdateClass, containedProperties, toUpdate, cascade,
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
	 * @return vertex for given value. May be null in some rare cases (typically obtaining a vertex that doesn't exist yet for deleting it)
	 */
	public Vertex getVertexFor(Object value, CascadeType cascade, Map<String, Object> objectsBeingUpdated) {
		boolean allowIdGeneration = CascadeType.PERSIST.equals(cascade) || CascadeType.MERGE.equals(cascade);
		// Here we suppose the service is the right one for the job (which may
		// not be the case)
		if (containedClass.isInstance(value)) {
			return getVertexForInstanceOfDataType(value, cascade, objectsBeingUpdated, allowIdGeneration);
		}
		Class<? extends Object> valueClass = value.getClass();
		if (repository.containsKey(valueClass)) {
			FinderCrudService service = repository.get(valueClass);
			if (service instanceof AbstractBluePrintsBackedFinderService) {
				return ((AbstractBluePrintsBackedFinderService) service).getVertexFor(value, cascade, objectsBeingUpdated);
			} else {
				throw new IncompatibleServiceException(service, valueClass);
			}
		} else if (Literals.containsKey(valueClass)) {
			return getVertexForLiteral(value, cascade);
		} else if (Tuples.containsKey(valueClass)) {
			return getVertexForTuple(value, cascade, objectsBeingUpdated);
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
	 * Get vertex for an instance of this service {@link #getContainedClass()}
	 * @param value value for which we search the vertex
	 * @param cascade current cascade type
	 * @param objectsBeingUpdated
	 * @param allowIdGeneration true if null id should be replaced by a new one
	 * @return a vertex for an instance of the given object. Under some rare circumstances, this vertex may be null.
	 */
	protected Vertex getVertexForInstanceOfDataType(Object value, CascadeType cascade, Map<String, Object> objectsBeingUpdated, boolean allowIdGeneration) {
		// was there any vertex prior to that call ? (don't worry, it will be used later)
		Vertex existing = getIdVertexFor(containedClass.cast(value), false);
		Vertex returned = null;
		if(existing==null) {
			returned = getIdVertexFor(containedClass.cast(value), allowIdGeneration);
		} else {
			returned = existing;
		}
		if (returned == null) {
			/*
			 * We als test cascade type here because we absolutely don't want to create vertices during search (as bug https://github.com/Riduidel/gaedo/issues/46 exposed)
			 */
			if(CascadeType.PERSIST==cascade || CascadeType.MERGE==cascade) {
				doUpdate(containedClass.cast(value), cascade, objectsBeingUpdated);
			}
			returned = getIdVertexFor(containedClass.cast(value), allowIdGeneration);
		} else {
			/* 
			 * vertex already exist, but maybe object needs an update.
			 * This can happen if MERGE has been set (directly or not), but also when cascading creations with @GeneratedValue set.
			 * Indeed, in such a case, previous call to #getIdVertexFor will create a vertex with a "good" id, which will put us in this very case.
			 * BUT cascade will be PERSIST, which is not supported. As a consequence, to avoid UPDATE on CREATE and support cascaded CREATE
			 * we check (when cascade is create) if existing vertex (that's to say the one priori to id generation) exist. If not, we cascade.
			 */
			if (CascadeType.MERGE == cascade ||(CascadeType.PERSIST==cascade && existing==null)) {
				doUpdate(containedClass.cast(value), cascade, objectsBeingUpdated);
			}
		}
		return returned;
	}

	protected Vertex getVertexForTuple(Object value, CascadeType cascade, Map<String, Object> objectsBeingUpdated) {
		return GraphUtils.getVertexForTuple(this, repository, value, cascade, objectsBeingUpdated);
	}

	/**
	 * Get vertex for a given literal
	 * @param value value to get
	 * @param cascade cascade mode
	 * @return the vertex associated to that literal, or null if cascade mode prevented that cascade loading.
	 * @see GraphUtils#getVertexForLiteral(GraphDatabaseDriver, Object)
	 */
	protected Vertex getVertexForLiteral(Object value, CascadeType cascade) {
		return GraphUtils.getVertexForLiteral(getDriver(), value, cascade);
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
		Vertex objectVertex = loadVertexFor(objectVertexId, containedClass.getName());
		return persister.loadObject(this, objectVertexId, objectVertex, objectsBeingAccessed);
	}

	/**
	 * Load veretx associated to given object id.
	 * 
	 * @param objectVertexId
	 *            vertex id for which we want a vertex
	 * @param className class name used for value. This parameter is mainly useful to disambiguate values.
	 * @return loaded vertex if found, or an exception (I guess ?) if none found
	 */
	public abstract Vertex loadVertexFor(String objectVertexId, String className);

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
		String vertexIdValue = strategy.getAsId(id[0]);
		Vertex rootVertex = loadVertexFor(vertexIdValue, containedClass.getName());
		if (rootVertex == null) {
            // root vertex couldn't be found directly, mostly due to
            // https://github.com/Riduidel/gaedo/issues/11
            // So perform the longer (but always working) query
            return find().matching(new QueryBuilder<InformerType>() {

                @Override
                public QueryExpression createMatchingExpression(InformerType informer) {
                    Collection<QueryExpression> ands = new LinkedList<QueryExpression>();
                    int index=0;
                    for(Property idProperty : strategy.getIdProperties()) {
                        ands.add(informer.get(idProperty.getName()).equalsTo(id[index++]));
                    }
                    return Expressions.and(ands.toArray(new QueryExpression[ands.size()]));
                }
            }).getFirst();
		} else {
			// root vertex can be directly found ! so load it immediatly
			return loadObject(vertexIdValue, new TreeMap<String, Object>());
		}
	}

	@Override
	public Collection<Property> getIdProperties() {
		return strategy.getIdProperties();
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
			strategy.assignId(value, id);
			if (getIdVertexFor(value, false /*
											 * no id generation when assigning
											 * an id !
											 */) == null) {
				try {
					TransactionalOperation<Boolean, DataType, InformerType> operation = new TransactionalOperation<Boolean, DataType, InformerType>(this) {

						@Override
						protected Boolean doPerform() {
							String idVertexId = getIdVertexId(value, strategy.isIdGenerationRequired());
							Vertex returned = getDriver().createEmptyVertex(value.getClass(), idVertexId, value);
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
	 * @return the requiresIdGeneration
	 * @category getter
	 * @category requiresIdGeneration
	 */
	public boolean isRequiresIdGeneration() {
		return strategy.isIdGenerationRequired();
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


	@Override
	protected QueryStatement<DataType, InformerType> createQueryStatement(QueryBuilder<InformerType> query) {
		return new GraphQueryStatement<DataType, InformerType>(query,
						this, repository);
	}

	/**
	 * @return the strategy
	 * @category getter
	 * @category strategy
	 */
	public GraphMappingStrategy<DataType> getStrategy() {
		return strategy;
	}

	@Override
	public SortedSet<String> getLens() {
		return lens;
	}

	/**
	 * Set current lens. Notice when {@link #lens} is changed strategy is updated.
	 * @param lens
	 */
	public void setLens(SortedSet<String> lens) {
		SortedSet<String> usedLens = new TreeSet<String>(lens);
		this.lens = Collections.unmodifiableSortedSet(usedLens);
		this.strategy.reloadWith(this);
	}
	
	/**
	 * Informer factory has been made public to allow use of lens in finders
	 * @param informerFactory
	 * @see com.dooapp.gaedo.finders.root.AbstractFinderService#setInformerFactory(com.dooapp.gaedo.finders.root.InformerFactory)
	 */
	@Override
	public void setInformerFactory(InformerFactory informerFactory) {
		super.setInformerFactory(informerFactory);
	}
}
