package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.LazyLoadable;
import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Entity filler will take field from object and put them in the entity in the
 * most correct way. Notice that, since this class provides direct access to Google Datastore, it is not accessible to external code.
 * 
 * @author Nicolas
 * 
 */
class EntityFiller<DataType, InformerType extends Informer<DataType>>
		implements EntityObjectMapper<DataType> {
	/**
	 * Service repository holding together all datastore servcies
	 */
	private final ServiceRepository repository;
	
	/**
	 * Google datastore
	 */
	private DatastoreService datastore;

	public EntityFiller(
			
			ServiceRepository repository,
			DatastoreService datastore) {
		this.repository = repository;
		this.datastore = datastore;
	}

	/**
	 * When mapping, some particular cases are handled. If value type is a
	 * collection type, some specific magick is done. If value type is managed
	 * by a service, we ensure value for object is already persisted, elsewhere
	 * we persist it. Then we ask its key to the appropriate service and store
	 * it.
	 */
	@Override
	public void map(Entity entity, DataType object, String key, Property value) {
		try {
			Type valueType = value.getGenericType();
			Object valueForObject = null;
			valueForObject = value.get(object);
			fillEntity(entity, key, valueType, valueForObject);
		} catch (Exception e) {
			throw new UnableToGetFieldException(e, value);
		}
	}

	/**
	 * Fill entity with infos from given property
	 * 
	 * @param entity
	 *            output entity
	 * @param key
	 *            save key
	 * @param value
	 *            value field
	 * @param valueForObject
	 *            value for the given object
	 * @throws IllegalAccessException
	 */
	private void fillEntity(Entity entity, String key, Type valueType,
			Object valueForObject) throws IllegalAccessException {
		if (valueType instanceof Class) {
			Class cls = (Class) valueType;
			if(cls.isEnum()) {
				// For an enum, we only need its name (obfuscation won't be supported here, so long proGuard !)
				entity.setProperty(key, ((Enum) valueForObject).name());
			} else {
				if (repository
						.containsKey((Class<?>) valueType)
						&& valueForObject != null) {
					valueForObject = getValueFromDomain((Class<?>) valueType,
							valueForObject);
				}
				entity.setProperty(key, valueForObject);
			}
		} else if (valueType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) valueType;
			if (Collection.class.isAssignableFrom((Class<?>) parameterizedType
					.getRawType())) {
				// A collection is genericized using only one type argument
				setCollectionProperties(entity, key, parameterizedType
						.getActualTypeArguments()[0],
						(Collection<?>) valueForObject);
			} else if (Map.class.isAssignableFrom((Class<?>) parameterizedType
					.getRawType())) {
				// A collection is genericized using only one type argument
				setMapProperties(entity, key, parameterizedType
						.getActualTypeArguments()[0], parameterizedType
						.getActualTypeArguments()[1],
						(Map<?, ?>) valueForObject);
			}

		}
	}

	/**
	 * Create object sub-entities for each collection element. The main issue of
	 * this method is to not fall into the hell of re-writing all elements each
	 * time, while not forgeting to write anyone.
	 * 
	 * @param entity
	 *            output entity
	 * @param key
	 *            entity base key
	 * @param value
	 *            field representing the collection
	 * @param valueForObject
	 *            value for this object (in this case, it is a
	 *            {@link Collection})
	 * @throws IllegalAccessException
	 */
	private void setCollectionProperties(Entity entity, String key,
			Type valueType, Collection<?> valueForObject)
			throws IllegalAccessException {
		Key root = entity.getKey();
		if(valueForObject==null) {
			fillEntity(entity, key + Utils.SIZE, Integer.class, 0);
		} else {
			fillEntity(entity, key + Utils.SIZE, Integer.class, valueForObject
					.size());
			if(((valueForObject instanceof LazyLoadable) && ((LazyLoadable) valueForObject).isLoaded()) || !(valueForObject instanceof LazyLoadable)) {
				int index = 0;
				for (Object element : valueForObject) {
					// Keys use 1-starting indexes !
					Key current = KeyFactory.createKey(root, key, index + 1);
					Entity nodeEntity = new Entity(current);
					fillEntity(nodeEntity, Utils.COLLECTION_VALUE_PROPERTY, valueType, element);
					datastore.put(nodeEntity);
					index++;
				}
			}
		}
	}

	private void setMapProperties(Entity entity, String key, Type keyType,
			Type valueType, Map<?, ?> valueForObject)
			throws IllegalAccessException {
		Key root = entity.getKey();
		if(valueForObject==null) {
			fillEntity(entity, key + Utils.SIZE, Integer.class, 0);
		} else {
			fillEntity(entity, key + Utils.SIZE, Integer.class, valueForObject
					.size());
			if(((valueForObject instanceof LazyLoadable) && ((LazyLoadable) valueForObject).isLoaded()) || !(valueForObject instanceof LazyLoadable)) {
				int index = 0;
				for (Map.Entry<?, ?> element : valueForObject.entrySet()) {
					// Keys use 1-starting indexes !
					Key current = KeyFactory.createKey(root, key, index + 1);
					Entity nodeEntity = new Entity(current);
					fillEntity(nodeEntity, Utils.MAP_KEY_PROPERTY, keyType, element.getKey());
					fillEntity(nodeEntity, Utils.MAP_VALUE_PROPERTY, valueType, element.getValue());
					datastore.put(nodeEntity);
					index++;
				}
			}
		}
	}

	/**
	 * Here, the DataType may be confusin (it even confuses me) since we call a
	 * method from another service by using the same DataType. However, never
	 * forget that a cast is always removed after compilation, it's just an
	 * artifice.
	 * 
	 * @param valueType
	 * @param valueForObject
	 * @return
	 * @throws IllegalAccessException
	 */
	/*
	 * For a strange reason compiling this with maven fails with a mysterious
	 * message: inconvertible types found
	 * 
	 * Fast and ugly solution was obviously to get rid of generics
	 */
	@SuppressWarnings("unchecked")
	private Object getValueFromDomain(Class<?> valueType, Object valueForObject)
			throws IllegalAccessException {
		DatastoreFinderService valueService = (DatastoreFinderService) repository
				.get(valueType);
		DataType typedValue = (DataType) valueForObject;
		if(!(valueService.getIdManager().hasKey(valueService.getKind(), typedValue))) {
			typedValue = (DataType) valueService.create(typedValue);
		}
		valueForObject = valueService.getIdManager().getKey(valueService.getKind(), typedValue);
		return valueForObject;
	}
}