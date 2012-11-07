package com.dooapp.gaedo.blueprints.strategies;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.root.FieldInformerLocator;
import com.dooapp.gaedo.properties.Property;

public class GraphDelegatingInformerLocator implements FieldInformerLocator {

	@Override
	public FieldInformer getInformerFor(Property field) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + GraphDelegatingInformerLocator.class.getName()
						+ "#getInformerFor has not yet been implemented AT ALL");
	}

	@Override
	public FieldInformer getInformerFor(Class informedClass, String fieldName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + GraphDelegatingInformerLocator.class.getName()
						+ "#getInformerFor has not yet been implemented AT ALL");
	}

}
