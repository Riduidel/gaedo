package com.dooapp.gaedo.extensions.id;

import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.Property;

public abstract class AbstractGenerator<IdType, DataType> {

	protected final IdBasedService<DataType> service;
	protected final Property idProperty;

	public AbstractGenerator(IdBasedService<DataType> service, Property idProperty) {
		this.service = service;
		this.idProperty = idProperty;
	}

	public void generateIdFor(DataType value) {
		IdType id = null;
		do {
			do {
				id = findNextId();
			} while(requiresNextId(id));
			// Continue while condition below fails, in other words stop while content should evaluate to false when assignId evaluates to true
		} while(service.assignId(value, id)==false);
	}

	/**
	 * Should return true if another id is required
	 * @param id id to test
	 * @return true if id exist in service false elsewhen
	 */
	protected boolean requiresNextId(IdType id) {
		try {
			return service.findById(id)!=null;
		} catch(Exception e) {
			return false;
		}
	}

	protected abstract IdType findNextId();
}
