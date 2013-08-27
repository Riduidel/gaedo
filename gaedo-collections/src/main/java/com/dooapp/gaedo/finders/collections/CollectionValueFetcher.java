package com.dooapp.gaedo.finders.collections;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.projection.ValueFetcher;
import com.dooapp.gaedo.properties.Property;

public class CollectionValueFetcher<DataType> implements ValueFetcher {

	private DataType sourceData;

	public CollectionValueFetcher(DataType element) {
		this.sourceData = element;
	}

	@Override
	public <Type> Type getValue(FieldInformer<Type> propertyDescriptor) {
		Iterable<Property> path = propertyDescriptor.getFieldPath();
		Object valueCache = sourceData;
		for(Property p : path) {
			valueCache = p.get(valueCache);
		}
		return (Type) valueCache;
	}

}
