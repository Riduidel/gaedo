package com.dooapp.gaedo.google.datastore.id;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Id;

import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.finders.id.BadIdAnnotatedClassException;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.google.datastore.IdManager;
import com.dooapp.gaedo.google.datastore.NoSuchIdManagerException;
import com.dooapp.gaedo.google.datastore.hierarchy.HierarchyManager;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.google.appengine.api.datastore.DatastoreService;

public class IdManagerFactory {
	private static final Logger logger = Logger.getLogger(IdManagerFactory.class.getName());

	/**
	 * Crezates an id manager for the given class and datastore
	 * 
	 * @return
	 */
	public static IdManager createIdManager(Class<?> containedClass,
			DatastoreService datastore, ServiceRepository repository, PropertyProvider provider, HierarchyManager hierarchyManager) {
		logger.config("defining id manager");
		Property id = AnnotationUtils.locateIdField(provider, containedClass, Long.TYPE, Long.class, String.class);
		IdManager returned = null;
		if (Long.class.equals(id.getType())
						|| Long.TYPE.equals(id.getType())) {
			returned = new LongIdManager(containedClass, datastore, provider, repository, hierarchyManager);
		}
		if (returned == null)
			throw new BadIdAnnotatedClassException(containedClass);
		return returned;
	}

}
