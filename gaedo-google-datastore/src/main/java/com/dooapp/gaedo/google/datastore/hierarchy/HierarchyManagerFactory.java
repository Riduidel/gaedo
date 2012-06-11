package com.dooapp.gaedo.google.datastore.hierarchy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.id.AnnotationsFinder;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.google.appengine.api.datastore.DatastoreService;

public class HierarchyManagerFactory {
	private static final Logger logger = Logger.getLogger(HierarchyManagerFactory.class.getName());
	
	public static HierarchyManager createHierarchyManager(Class<?> containedClass,
			DatastoreService datastore, ServiceRepository repository, PropertyProvider provider) {
		Property[] containedClassProperties = provider.get(containedClass);
		List<Property> possibleParents = AnnotationsFinder.findAll(containedClassProperties, Annotations.PARENT);
		List<Property> possibleChildren = AnnotationsFinder.findAll(containedClassProperties, Annotations.CHILD);
		Collection<String> errors = new LinkedList<String>();
		if(possibleParents.size()>1)
			errors.add("class "+containedClass.getName()+" declares more than one field as parent : "+possibleParents);
		if(errors.size()>0)
			throw new ImpossibleToCreateHierarchyManagerException(containedClass, errors);
		return new DefaulHierarchyManagerImpl(repository, possibleParents.size()>0 ? possibleParents.get(0) : null, possibleChildren);
		
	}
}
