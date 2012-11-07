package com.dooapp.gaedo.google.datastore.hierarchy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.google.datastore.DatastoreFinderService;
import com.dooapp.gaedo.google.datastore.IdManager;
import com.dooapp.gaedo.google.datastore.NonStoredParentException;
import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.Key;

public class DefaulHierarchyManagerImpl implements HierarchyManager {
	private final ServiceRepository repository;

	/**
	 * Field holding the parent of current data type
	 */
	private final Property parentField;

	/**
	 * Collection of all fields for which the child annotation has been set.
	 * Elements of this collection which are elements of classes that are managed by services wil
	 */
	private final Collection<Property> childrenFields;
	
	public DefaulHierarchyManagerImpl(ServiceRepository repository, Property parentField, List<Property> possibleChildren) {
		this.repository = repository;
		this.parentField = parentField;
		this.childrenFields = Collections.unmodifiableCollection(new LinkedList<Property>(possibleChildren));
	}

	@Override
	public Key getParentKey(Object data) {
		if (!repository.containsKey(parentField.getType())) {
			throw new NonStoredParentException(parentField);
		}
		DatastoreFinderService parentService = (DatastoreFinderService) repository
				.get(parentField.getType());
		IdManager parentIdManager = parentService.getIdManager();
		Object parentObject = parentField.get(data);
		String parentKind = parentService.getKind();
		if (!parentIdManager.hasKey(parentKind, parentObject)) {
			parentIdManager.createKey(parentKind, parentObject);
		}
		return parentIdManager.getKey(parentKind, parentObject);
	}

	@Override
	public boolean hasParent() {
		return parentField!=null;
	}

}
