package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;

public class LowerThan<ComparableType extends Comparable<ComparableType>> extends ComparableValuedVertexTest<ComparableType> implements VertexTest {

	public LowerThan(ServiceRepository repository, Iterable<Property> p, ComparableType value, boolean strictly) {
		super(repository, p, value, strictly);
	}

	public boolean doCompare(ComparableType effective) {
		return strictly ? effective.compareTo(expected)<0 : effective.compareTo(expected)<=0;
	}

}
