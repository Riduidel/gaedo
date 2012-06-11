package com.dooapp.gaedo.google.datastore.id;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.Id;

import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.google.datastore.DatastoreFinderService;
import com.dooapp.gaedo.google.datastore.IdManager;
import com.dooapp.gaedo.google.datastore.NonStoredParentException;
import com.dooapp.gaedo.google.datastore.TooManyParentsException;
import com.dooapp.gaedo.google.datastore.UnableToGetFieldException;
import com.dooapp.gaedo.google.datastore.UnableToSetFieldException;
import com.dooapp.gaedo.google.datastore.hierarchy.HierarchyManager;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;

public class LongIdManager implements IdManager {
	/**
	 * Field holding the long id used
	 */
	private Property idField;

	private HierarchyManager hierarchyManager;
	/**
	 * Google datastore, used as an id allocator
	 */
	private final DatastoreService datastore;
	/**
	 * Service repository used to access to parent object metadata
	 */
	private ServiceRepository repository;

	public LongIdManager(Class<?> containedClass, DatastoreService datastore,
			PropertyProvider provider, ServiceRepository repository, HierarchyManager hierarchyManager) {
		this.datastore = datastore;
		this.repository = repository;
		this.hierarchyManager = hierarchyManager;
		Class<?> current = containedClass;
		Property[] fields = provider.get(current);
		idField = AnnotationUtils.locateIdField(provider, containedClass, Long.TYPE, Long.class);
	}

	/**
	 * Creates a key for the given
	 * 
	 * @param data
	 */
	public void createKey(String kind, Object data) {
		Key key;
		try {
			KeyRange range = createIdRange(kind, data);
			key = range.getStart();
			// This is a 1-length range, so getting start value is by far
			// enough
			idField.set(data, key.getId());
		} catch (Exception e) {
			throw new UnableToSetFieldException(e, idField);
		}
	}

	/**
	 * When creating an id range, we first check if field has a parent. If it is
	 * the case, things go rather complicated (since parent may have to be
	 * saved). Elsewhere, it's quite simple :-)
	 * 
	 * @param kind
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private KeyRange createIdRange(String kind, Object data) {
		if (!hierarchyManager.hasParent()) {
			return datastore.allocateIds(kind, 1);
		} else {
			return datastore.allocateIds(hierarchyManager.getParentKey(data), kind, 1);
		}
	}

	public Property getId() {
		return idField;
	}

	/**
	 * Get the id for the given object. This method can be overriden to return
	 * any type of id, provided user code takes care of it.
	 * 
	 * @param data
	 * @return the long value of id
	 * @throws IllegalAccessException
	 */
	public Object getId(Object data) {
		try {
			return (Long) idField.get(data);
		} catch (Exception e) {
			throw new UnableToGetFieldException(e, idField);
		}
	}

	/**
	 * Get key associated to given object. A key is always constructed the
	 * following way
	 * 
	 * @param data
	 *            input object typed as object to be callable by unknowers of
	 *            the given datatype (as an example, {@link com.dooapp.gaedo.google.datastore.EntityFiller}), when
	 *            using domain navigation features
	 * @return the key for that object
	 * @throws IllegalAccessException
	 */
	public Key getKey(String kind, Object data) {
		return buildKey(kind, getId(data));
	}

	public void setKey(String kind, Key key, Object returned) {
		try {
			idField.set(returned, key.getId());
		} catch (Exception e) {
			throw new UnableToSetFieldException(e, idField);
		}
	}

	@Override
	public boolean hasKey(String kind, Object data) {
		Long id = (Long) getId(data);
		return id!=null && id > 0;
	}

	@Override
	public boolean isIdField(Property field) {
		return idField.equals(field);
	}

	@Override
	public Key buildKey(String kind, Object value) {
		return KeyFactory.createKey(kind, (Long) value);
	}

	public Property getIdField() {
		return idField;
	}
}
