package com.dooapp.gaedo.google.datastore;

import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheListener;
import javax.cache.CacheManager;

import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.extensions.migrable.VersionMigratorFactory;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.id.AnnotationsFinder;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.AbstractFinderService;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.google.datastore.hierarchy.HierarchyManager;
import com.dooapp.gaedo.google.datastore.hierarchy.HierarchyManagerFactory;
import com.dooapp.gaedo.google.datastore.id.IdManagerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.PropertyProviderUtils;
import com.dooapp.gaedo.utils.UnableToCreateObjectException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * Base class for services connecting to google datastore without any
 * sophisticated API. Notice this class is a cache listener to receive objects
 * invalidation for local cache
 * 
 * @author ndx
 * 
 * @param <DataType>
 * @param <InformerType>
 */
public class DatastoreFinderServiceImpl<DataType, InformerType extends Informer<DataType>>
		extends AbstractFinderService<DataType, InformerType> implements
		DatastoreFinderService<DataType, InformerType>, CacheListener {

	private static final String CLASS_PROPERTY = "_dynamic_class_";

	/**
	 * Key used for eviction across instances
	 */
	private static final String KEY_TO_EVICT = DatastoreFinderServiceImpl.class
			.getName()
			+ ".KEY_TO_EVICT";

	public static final Logger logger = Logger
			.getLogger(DatastoreFinderServiceImpl.class.getName());

	/**
	 * Cache used to invalidate elements after an update or delete
	 */
	private Cache cache = null;

	/**
	 * Connection service to google datastore
	 */
	public static final DatastoreService datastore = DatastoreServiceFactory
			.getDatastoreService();

	/**
	 * Id manager is responsible for all id management operations.
	 */
	private IdManager idManager;

	/**
	 * Access to parent object, used when creating a new object
	 */
	private Property parentField;

	private HierarchyManager hierarchyManager;

	/**
	 * Maps alls the objects created by this service. Weak reference is used to
	 * ensure this service don't hold long-terme reference to objects, and also
	 * that objects are freely unreferenced
	 */
	protected Map<Key, WeakReference<DataType>> objectsBeingAccessed = new HashMap<Key, WeakReference<DataType>>();

	/**
	 * Accelerator cache linking classes objects to the field names map used to
	 * persist those fields. only used by the
	 * {@link #map(Entity, Object, EntityObjectMapper)} method.
	 */
	protected Map<Class<?>, Map<String, Property>> classes = new HashMap<Class<?>, Map<String, Property>>();

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

	public DatastoreFinderServiceImpl(Class<DataType> containedClass,
			Class<InformerType> informerClass,
			ProxyBackedInformerFactory proxyInformerFactory,
			ServiceRepository repository,
			PropertyProvider provider) {
		this(containedClass, informerClass, proxyInformerFactory, repository, provider,
				HierarchyManagerFactory.createHierarchyManager(containedClass, datastore, repository, provider));
	}

	protected DatastoreFinderServiceImpl(Class<DataType> containedClass,
			Class<InformerType> informerClass,
			ProxyBackedInformerFactory proxyInformerFactory,
			ServiceRepository repository,
			PropertyProvider provider,
			HierarchyManager hierarchyManager) {
		this(containedClass, informerClass, proxyInformerFactory, repository, provider, hierarchyManager,
				IdManagerFactory.createIdManager(containedClass, datastore, repository, provider, hierarchyManager));
	}

	protected DatastoreFinderServiceImpl(Class<DataType> containedClass,
			Class<InformerType> informerClass,
			ProxyBackedInformerFactory proxyInformerFactory,
			ServiceRepository repository,
			PropertyProvider provider, 
			HierarchyManager hierarchyManager, IdManager idManager) {
		super(containedClass, informerClass, proxyInformerFactory);
		this.repository = repository;
		this.propertyProvider = provider;
		this.hierarchyManager = hierarchyManager;
		this.idManager = idManager;
		// If there is an issue with parent field, it should had been seen by id manager
		List<Property> parents = AnnotationsFinder.findAll(provider.get(containedClass), Annotations.PARENT);
		if(parents.size()==1)
			this.parentField = parents.get(0);
		this.migrator = VersionMigratorFactory.create(containedClass);
		if (logger.isLoggable(Level.INFO))
			logger.info("datastore finder for " + containedClass.getName()
					+ " uses id manager of class"
					+ idManager.getClass().getName());

		try {
			cache = CacheManager.getInstance().getCacheFactory().createCache(
					Collections.emptyMap());
			cache.addListener(this);
			logger.info("cache started successfully !");
		} catch (CacheException e) {
			// If the previous fails, cache is non observable, and this object
			// won't get updated ... not cool, but not tragical
			// We degrade gracefully by not using cache features (which means ==
			// may no more work
			logger.warning("cache failed to start. == WON'T work !");
		}
	}

	/**
	 * Create an object of the given type in the datastore, and return a
	 * reference to the stored object (may not be == to toCreate, but will be
	 * {@link Object#equals(Object)})
	 */
	@Override
	public DataType create(DataType toCreate) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("creating object " + toCreate);
		}
		if(parentField!=null) {
			// Ensure parent exists. For that, we check if this parent has a key. if it is not the case, the parent has never been saved.
			DatastoreFinderService parentService = (DatastoreFinderService) repository.get(parentField.getType());
			Object parentObject = parentField.get(toCreate);
			if(parentObject==null) {
				throw new ImpossibleToSaveObjectWithNullParentException(toCreate, containedClass, parentField);
			}
			if(!parentService.getIdManager().hasKey(parentService.getKind(), parentObject)) {
				parentObject = parentService.create(parentObject);
				parentField.set(toCreate, parentObject);
			}
		}
		Key key = datastore.put(getEntity(toCreate, false));
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("it has key " + key);
		}
		try {
			return getObjectFromKey(key);
		} finally {
			// Yup^, getEntity,when given the false boolean value, puts the
			// object in that infamous map
			// notice we remove it in the finally block, to ensure it is done
			// after all elements have been created
			removeAccessed(key);
		}
	}

	@Override
	protected QueryStatement<DataType, InformerType> createQueryStatement(
			QueryBuilder<InformerType> query) {
		return new DirectDatastoreQueryStatement<DataType, InformerType>(query,
				this, datastore, repository);
	}

	/**
	 * When performing a deletion, we start by getting all child nodes to add them to the delete list (cascade delete indeed does not seems to be natively
	 * integrated to GAE - look at http://code.google.com/intl/fr-FR/appengine/docs/java/datastore/relationships.html#Dependent_Children_and_Cascading_Deletes
	 * for the scary details).
	 */
	@Override
	public void delete(DataType toDelete) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("deleting object " + toDelete);
		}
		Key key = getEntity(toDelete, true).getKey();
		Collection<Key> keysToDelete = new LinkedList<Key>();
		keysToDelete.add(key);
		// Delete all children : both automatic ones (like collection indexes) and manual ones (object declared as children)
		Query toExecute = new Query(key);
		toExecute = toExecute.setKeysOnly();
		PreparedQuery prepared = datastore.prepare(toExecute);
		Iterable<Entity> result = prepared.asIterable();
		for(Entity e : result) {
			keysToDelete.add(e.getKey());
		}
		datastore.delete(keysToDelete);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("object " + toDelete + " has been deleted");
		}
		cache.put(KEY_TO_EVICT, key);
	}

	private void evict() {
		objectsBeingAccessed.remove(cache.get(KEY_TO_EVICT));
	}

	/**
	 * Create a query to get all objects, then execute the query and fetches all
	 * results
	 */
	@Override
	public Iterable<DataType> findAll() {
		Query used = new Query(getKind());
		PreparedQuery prepared = datastore.prepare(used);
		return new DataTypeIterable<DataType>(this, prepared.asIterable());
	}

	/**
	 * Create an entity from the input object data. Notice that if the associated data class has defined a parent element,
	 * we will first ensure this parent key exist in order to use it to create entity
	 * 
	 * @param data
	 * @param withKey
	 *            when set to true, the key value will be rebuilt. Elsewhere, it
	 *            will be forgotten
	 * @return
	 */
	private Entity getEntity(DataType data, boolean withKey) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("building entity (with key ? " + withKey
					+ ") from object " + data);
		}
		Entity returned;
		Key key;
		// key needs to be created. For easier code, we prefer to generate it
		// before. Notice key may have been created before if object is parent of another one
		if (!withKey) {
			if(!idManager.hasKey(getKind(), data))
				idManager.createKey(getKind(), data);
		}
		// Then, build the key from the known id
		key = idManager.getKey(getKind(), data);
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("object uses key " + key);
		}
		// Avoid stack overflows
		objectsBeingAccessed.put(key, new WeakReference<DataType>(data));
		// And create an entity with that key
		returned = new Entity(key);
		// A class field is generated to store live class (in order to be able
		// to instanciate object using that class, rather than containedClass
		returned
				.setProperty(CLASS_PROPERTY, data.getClass().getCanonicalName());
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("putting all data in the correct fields for " + data);
		}
		map(returned, data, new EntityFiller<DataType, InformerType>(
				repository, datastore));
		// Finally, if class supports migration, persists the field used as key
		if(migrator!=null) {
			String liveVersionFieldName = migrator.getLiveVersionFieldName();
			Property liveProperty = getInformer().get(liveVersionFieldName).getField();
			returned.setProperty(migrator.getPersistedVersionFieldName(), Utils.getDatastoreFieldName(liveProperty));
		}
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("all data of " + data + " has been put");
		}
		return returned;
	}

	public IdManager getIdManager() {
		return idManager;
	}

	/**
	 * get this service very specific key to evict
	 * 
	 * @return
	 */
	public String getKeyToEvict() {
		return KEY_TO_EVICT + "." + getKind();
	}

	/**
	 * Get the kind identifier for keys built by this service
	 * 
	 * @return
	 */
	public String getKind() {
		return containedClass.getName();
	}

	/**
	 * Get object associated to given entity
	 * 
	 * @param entity
	 * @return
	 */
	public DataType getObject(Entity entity) {
		if(!getKind().equals(entity.getKind())) {
			// Try to lookup parents
			Key input = entity.getKey();
			Key current = input;
			while(current!=null && !(getKind().equals(current.getKind()))) {
				current = current.getParent();
			}
			if(current==null) {
				throw new UnableToCreateObjectDueToBadKeyException(input, containedClass);
			}
			try {
				entity = datastore.get(current);
			} catch (EntityNotFoundException e) {
				throw new UnableToCreateObjectException(e, containedClass);
			}
		}
		// Load object from source entity
		try {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("getting object associated to  " + entity);
			}
			// Do some entity check to see if a migration is required
			if(migrator!=null) {
				String storedVersionField = entity.getProperty(migrator.getPersistedVersionFieldName()).toString();
				Object storedVersion = entity.getProperty(storedVersionField);
				Object liveVersion = migrator.getCurrentVersion();
				if(!storedVersion.equals(liveVersion)) {
					entity = migrator.migrate(this, entity, storedVersion, liveVersion);
				}
			}
			DataType returned;
			try {
				String dynamicClass = (String) entity
						.getProperty(CLASS_PROPERTY);
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("object should be a " + dynamicClass);
				}
				Class<?> instanciated = Class.forName(dynamicClass);
				returned = (DataType) instanciated.newInstance();
				objectsBeingAccessed.put(entity.getKey(),
						new WeakReference<DataType>(returned));
			} catch (Exception e) {
				throw new UnableToCreateObjectException(e, containedClass);
			}
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("mapping fields");
			}
			// Set all fields values
			map(entity, returned, new ObjectFiller<DataType, InformerType>(
					repository, datastore));
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("setting key");
			}
			// Now set id value
			idManager.setKey(getKind(), entity.getKey(), returned);
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("object is usable");
			}
			return returned;
		} finally {
			objectsBeingAccessed.remove(entity.getKey());
		}
	}

	/**
	 * Faster loop mechanism
	 * 
	 * @param entity
	 * @param object
	 * @param mapper
	 */
	private void map(Entity entity, DataType object,
			EntityObjectMapper<DataType> mapper) {
		Map<String, Property> fields = createFields(object.getClass());
		for (Map.Entry<String, Property> entry : fields.entrySet()) {
			mapper.map(entity, object, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Create the fields map used to store infos from this class.
	 * 
	 * @param clazz
	 *            class for which we want to have all fields
	 * @return a Map linking unique-made names for fields to the Field objects
	 */
	private Map<String, Property> createFields(Class<?> clazz) {
		Map<String, Property> returned = new TreeMap<String, Property>();
		if (!clazz.equals(Object.class)) {
			if (!classes.containsKey(clazz)) {
				Map<String, Property> fields = new TreeMap<String, Property>();
				for (Property f : PropertyProviderUtils.getAllProperties(propertyProvider, clazz)) {
					// Remove transient fields (as of issue #19)
					if (!f.hasModifier(Modifier.TRANSIENT) && !f.hasModifier(Modifier.STATIC))
						fields.put(Utils.getDatastoreFieldName(f), f);
				}
				classes.put(clazz, fields);
			}
			returned.putAll(classes.get(clazz));
			returned.putAll(createFields(clazz.getSuperclass()));
		}
		return returned;
	}

	/**
	 * Cache is cleared ? Clear local one !
	 */
	@Override
	public void onClear() {
		objectsBeingAccessed.clear();
	}

	/**
	 * Nothing to do on object eviction
	 */
	@Override
	public void onEvict(Object arg0) {
	}

	/**
	 * Saeme operation as put
	 */
	@Override
	public void onLoad(Object arg0) {
		if (KEY_TO_EVICT.equals(arg0)) {
			evict();
		}
	}

	@Override
	public void onPut(Object arg0) {
		if (KEY_TO_EVICT.equals(arg0)) {
			evict();
		}
	}

	/**
	 * Nothing to do when an element is removed
	 */
	@Override
	public void onRmove(Object arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * When updating an object, if GAE cache is inited, we put object key in
	 * cache for listeners to invalidate keuy locally. Elsewhere, object is
	 * simply removed from local cache
	 */
	@Override
	public DataType update(DataType toUpdate) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("updating object " + toUpdate);
		}
		Key key = datastore.put(getEntity(toUpdate, true));
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("it has key " + key);
		}
		try {
			return getObjectFromKey(key);
		} finally {
			if (cache == null) {
				// Yup^, getEntity,when given the false boolean value, puts the
				// object in that infamous map
				// notice we remove it in the finally block, to ensure it is
				// done
				// after all elements have been created
				objectsBeingAccessed.remove(key);
			} else {
				cache.put(KEY_TO_EVICT, key);
			}
		}
	}

	/**
	 * Get object from key by first generating a datastore key then loading object associated to key (if any)
	 * @see IdManager#getKey(String, Object)
	 * @see #getObjectFromKey(Key)
	 */
	@Override
	public DataType findById(Object...id) {
		if(id.length!=0) {
			throw new BadIdArgumentException(id);
		} else if(!(id[0].getClass().equals(Long.class) || id[0].getClass().equals(Long.TYPE) ||
				id[0].getClass().equals(Integer.class) || id[0].getClass().equals(Integer.TYPE))) {
			throw new BadIdArgumentException(id);
		}
		return getObjectFromKey(getIdManager().getKey(getKind(), id[0]));
	}

	/**
	 * {@inheritDoc}
	 * @see IdManager#getIdField()
	 */
	@Override
	public Collection<Property> getIdProperties() {
		return Arrays.asList(getIdManager().getIdField());
	}

	public Property getParentField() {
		return parentField;
	}

	@Override
	public String toString() {
		StringBuilder sOut = new StringBuilder();
		sOut.append(getClass().getName()).append(" persistence service for "+containedClass.getName()+" informed by "+informerClass.getName()).append("\n");
		sOut.append("ids are expected to be stored by ").append(getIdManager().getIdField()).append(" and parent/children is handled by ").append(parentField).append("\n");
		for(DataType element : findAll()) {
			sOut.append(element.toString()).append("\n");
		}
		return sOut.toString();
	}

	protected DataType loadObject(Key key) {
		try {
			return getObject(datastore.get(key));
		} catch (EntityNotFoundException e) {
			throw new EntityDoesNotExistsException(e, key);
		}
	}

	/**
	 * Remove that key from the collection of accessed object. In other words, it is no more considered as server-read and can live it life free
	 * @param key
	 */
	protected void removeAccessed(Key key) {
		objectsBeingAccessed.remove(key);
	}

	/**
	 * Get object associated to given key. Notice this method uses internal
	 * cache ({@link #objectsBeingAccessed}) before to resolve call on
	 * datastore.
	 * 
	 * @param key
	 * @return
	 */
	public DataType getObjectFromKey(Key key) {
		if (objectsBeingAccessed.containsKey(key)) {
			DataType returned = objectsBeingAccessed.get(key).get();
			if (returned == null) {
				objectsBeingAccessed.remove(key);
			} else {
				return returned;
			}
		}
		return loadObject(key);
	}

	@Override
	public boolean assignId(DataType value, Object... id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+IdBasedService.class.getName()+"#assignId has not yet been implemented AT ALL - as id generation is done in a standalone fashion");
	}
}
