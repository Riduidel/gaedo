package com.dooapp.gaedo.blueprints.operations;

import java.util.Collection;

import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.properties.Property;

public class CollectionSizeProperty extends AbstractPropertyAdapter {

	public CollectionSizeProperty(Property p) {
		super();
		setDeclaringClass(p.getDeclaringClass());
		setName(getName(p));
		setGenericType(Integer.TYPE);
		copyAnnotationsFrom(p);
	}

	public static String getName(Property p) {
		return getNameFor(p.getName());
	}

	public static String getNameFor(String propertyName) {
		return propertyName+".size";
	}

	@Override
	public String toGenericString() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + CollectionSizeProperty.class.getName() + "#toGenericString has not yet been implemented AT ALL");
	}

	@Override
	public Object get(Object bean) {
		return ((Collection) bean).size();
	}

	@Override
	public void set(Object bean, Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + CollectionSizeProperty.class.getName() + "#set has not yet been implemented AT ALL");
	}

	@Override
	public Object fromString(String value) {
		return Integer.parseInt(value);
	}

}
