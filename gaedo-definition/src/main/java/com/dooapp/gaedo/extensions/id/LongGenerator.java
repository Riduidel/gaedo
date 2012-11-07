package com.dooapp.gaedo.extensions.id;

import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.Property;

public class LongGenerator<DataType> extends AbstractGenerator<Long, DataType> implements IdGenerator<DataType>{

	private long seed;

	public LongGenerator(IdBasedService service, Property idProperty) {
		super(service, idProperty);
	}

	public void generateIdFor(DataType value) {
		seed = value.hashCode();
		super.generateIdFor(value);
	}

	@Override
	protected Long findNextId() {
		seed = seed+1l;
		return seed;
	}

}
