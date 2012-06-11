package com.dooapp.gaedo.extensions.id;

import java.util.UUID;

import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.Property;

public class StringGenerator<DataType> extends AbstractGenerator<String, DataType> implements IdGenerator<DataType> {

	public StringGenerator(IdBasedService service, Property idProperty) {
		super(service, idProperty);
	}
	
	public void generateIdFor(DataType value) {
		super.generateIdFor(value);
	}
	
	@Override
	protected String findNextId() {
		return UUID.randomUUID().toString();
	}
}
