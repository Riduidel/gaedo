package com.dooapp.gaedo.blueprints.strategies;

import java.util.Collection;
import java.util.Map;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.properties.Property;

public class GraphBasedMappingStrategy implements GraphMappingStrategy {

	@Override
	public Map<Property, Collection<CascadeType>> getContainedProperties(Object object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + GraphBasedMappingStrategy.class.getName()
						+ "#getContainedProperties has not yet been implemented AT ALL");
	}

}
