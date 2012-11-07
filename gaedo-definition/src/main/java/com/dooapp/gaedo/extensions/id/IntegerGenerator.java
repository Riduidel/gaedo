package com.dooapp.gaedo.extensions.id;

import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.Property;

public class IntegerGenerator<DataType> extends AbstractGenerator<Integer, DataType> implements IdGenerator<DataType>{

	private int seed;

	public IntegerGenerator(IdBasedService service, Property idProperty) {
		super(service, idProperty);
	}
	
	public void generateIdFor(DataType value) {
		seed = value.hashCode();
		super.generateIdFor(value);
	}

	@Override
	protected Integer findNextId() {
		return seed++;
	}

}
