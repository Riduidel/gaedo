package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.LazyLoadable;
import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.apphosting.api.DatastorePb.CompiledQuery.EntityFilter;

/**
 * Dual operation of the one performed by {@link EntityFilter}.
 * 
 * @author Nicolas
 * 
 */
public class ObjectFiller<DataType, InformerType extends Informer<DataType>>
		implements EntityObjectMapper<DataType> {
	private static final Logger logger = Logger.getLogger(ObjectFiller.class.getName());
	/**
	 * Service repository holding together all datastore servcies
	 */
	private final ServiceRepository repository;
	
	/**
	 * Google datastore
	 */
	private DatastoreService datastore;

	/**
	 * @param datastoreFinderService
	 */
	public ObjectFiller(
			ServiceRepository repository,
			DatastoreService datastore) {
		this.repository = repository;
		this.datastore = datastore;
	}

	@Override
	public void map(Entity entity, DataType object, String entityName,
			Property value) {
		try {
			Type valueType = value.getGenericType();
			Object castedProperty = getContainedProperty(entity, entityName,
					valueType, value.get(object));
			value.set(object, castedProperty);
		} catch (Exception e) {
			logger.log(Level.WARNING, "setting of field failed", new UnableToSetFieldException(e, value));
		}
	}

	/**
	 * Get the contained property
	 * 
	 * @param entity
	 *            source entity
	 * @param entityName
	 *            entity property name (in the entity considered as a hash)
	 * @param valueType
	 *            value type
	 * @param defaultvalue
	 *            default value, given as a hint (may be null)
	 * @return
	 */
	private Object getContainedProperty(Entity entity, String entityName,
			Type valueType, Object defaultvalue) {
		Object property = entity.getProperty(entityName);
		if (valueType instanceof Class<?>) {
			Object castedProperty = null;
			Class<?> valueClass = (Class<?>) valueType;
			if(valueClass.isEnum()) {
				// Stored value is a string
				castedProperty = Enum.valueOf((Class<? extends Enum>)valueClass, property.toString());
			} else {
				if (repository.containsKey(valueClass)
						&& property != null) {
					property = getValueFromDomain(valueClass, property);
				}
				castedProperty = Utils.getCastedProperty(valueClass,
						property);
			}
			return castedProperty;
		} else if (valueType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) valueType;
			Type rawContainerType = parameterizedType.getRawType();
			if (rawContainerType instanceof Class) {
				Class<?> rawContainerClass = (Class<?>) rawContainerType;
				if (Collection.class.isAssignableFrom(rawContainerClass)) {
					return setCollectionProperties(entity, entityName,
							parameterizedType.getActualTypeArguments()[0],
							com.dooapp.gaedo.utils.Utils.generateCollection(rawContainerClass,
									(Collection<?>) property),
							rawContainerClass);
				} else if (Map.class.isAssignableFrom(rawContainerClass)) {
					return setMapProperties(
							entity,
							entityName,
							parameterizedType.getActualTypeArguments()[0],
							parameterizedType.getActualTypeArguments()[1],
							com.dooapp.gaedo.utils.Utils.generateMap(rawContainerClass, (Map<?, ?>) property),
							rawContainerClass);
				}
			}
		}
		throw new UnsupportedOperationException("the case of fields like "
				+ valueType.toString() + " has not yet been handled !");
	}

	/**
	 * Load collection from entity in target object. This class loads collection
	 * by intercepting them with a lazy loading strategy.
	 * 
	 * @param entity
	 *            source entity
	 * @param fieldName
	 * @param rawContainerClass
	 */
	private Collection<?> setCollectionProperties(Entity entity,
			String fieldName, Type valueType, Collection toReturn,
			Class<?> rawContainerClass) {
		Key root = entity.getKey();
		int collectionSize = (Integer) Utils.getCastedProperty(Integer.TYPE,
				entity.getProperty(fieldName + Utils.SIZE));
		// since elements are 1-indexed in datastore, we have to use this
		// special loop construct
		Collection<Key> toGrab = new LinkedList<Key>();
		for (int index = 0; index < collectionSize; index++) {
			toGrab.add(KeyFactory.createKey(root, fieldName, index + 1));
		}
		return (Collection<?>) Proxy.newProxyInstance(getClass()
				.getClassLoader(), new Class<?>[] { rawContainerClass, LazyLoadable.class },
				new CollectionLazyLoader(valueType, toReturn, toGrab));
	}

	private Object setMapProperties(Entity entity, String fieldName,
			Type keyType, Type valueType, Map<?, ?> toReturn,
			Class<?> rawContainerClass) {
		Key root = entity.getKey();
		int collectionSize = (Integer) Utils.getCastedProperty(Integer.TYPE,
				entity.getProperty(fieldName + Utils.SIZE));
		// since elements are 1-indexed in datastore, we have to use this
		// special loop construct
		Collection<Key> toGrab = new LinkedList<Key>();
		for (int index = 0; index < collectionSize; index++) {
			toGrab.add(KeyFactory.createKey(root, fieldName, index + 1));
		}
		return (Map<?, ?>) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { rawContainerClass, LazyLoadable.class }, new MapLazyLoader(
						keyType, valueType, toReturn, toGrab));
	}

	private class MapLazyLoader implements InvocationHandler, LazyLoadable {
		private boolean loaded = false;

		private Type keyType;
		private Type valueType;
		private Map toReturn;
		private Collection<Key> keys;

		public MapLazyLoader(Type keyType, Type valueType, Map<?, ?> toReturn,
				Collection<Key> toGrab) {
			this.keyType = keyType;
			this.valueType = valueType;
			this.toReturn = toReturn;
			this.keys = toGrab;
		}

		private void load() {
			if(!loaded) {
				Map<Key, Entity> values = datastore
						.get(keys);
				for (Key source : keys) {
					Entity nodeEntity = values.get(source);
					Object containedKey = getContainedProperty(nodeEntity, Utils.MAP_KEY_PROPERTY,
							keyType, null);
					Object containedValue = getContainedProperty(nodeEntity,
							Utils.MAP_VALUE_PROPERTY, valueType, null);
					toReturn.put(containedKey, containedValue);
				}
				loaded = true;
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if(method.getDeclaringClass().equals(LazyLoadable.class)) {
				return method.invoke(this, args);
			}
			// Added to fix save inconsistency (see issue #33)
			if ("size".equals(method.getName()) && !loaded) {
				return keys.size();
			}
			// It's time to really load source collection
			if (toReturn.size() == 0)
				load();
			return method.invoke(toReturn, args);
		}

		@Override
		public boolean isLoaded() {
			return loaded;
		}

	}

	/**
	 * Well, the name says all, no ?
	 * 
	 * @author ndx
	 * 
	 */
	@SuppressWarnings("unchecked")
	private class CollectionLazyLoader implements InvocationHandler, LazyLoadable {
		private boolean loaded = false;

		private Type valueType;
		private Collection toReturn;
		private Collection<Key> keys;

		public CollectionLazyLoader(Type valueType, Collection toReturn,
				Collection<Key> keys) {
			this.valueType = valueType;
			this.toReturn = toReturn;
			this.keys = keys;
		}

		private void load() {
			if(!loaded) {
				Map<Key, Entity> values = datastore
						.get(keys);
				for (Key source : keys) {
					Entity nodeEntity = values.get(source);
					Object containedValue = getContainedProperty(nodeEntity,
							Utils.COLLECTION_VALUE_PROPERTY, valueType, null);
					toReturn.add(containedValue);
				}
				loaded = true;
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if(method.getDeclaringClass().equals(LazyLoadable.class)) {
				return method.invoke(this, args);
			}
			// Added to fix save inconsistency (see issue #33)
			if ("size".equals(method.getName()) && !loaded) {
				return keys.size();
			}
			// It's time to really load source collection
			if (toReturn.size() == 0)
				load();
			return method.invoke(toReturn, args);
		}

		@Override
		public boolean isLoaded() {
			return loaded;
		}
	}

	/**
	 * Load from one of the domain class the object associated to input key.
	 * 
	 * @param valueType
	 *            value type
	 * @param key
	 *            key associated to value
	 * @return the object, loaded from the correct service.
	 */
	/*
	 * For a strange reason compiling this with maven fails with a mysterious
	 * message: inconvertible types found
	 * 
	 * Fast and ugly solution was obviously to get rid of generics
	 */
	@SuppressWarnings("unchecked")
	private Object getValueFromDomain(Class<?> valueType, Object key) {
		DatastoreFinderService valueService = (DatastoreFinderService) repository
				.get(valueType);
		return valueService.getObjectFromKey((Key) key);
	}
}