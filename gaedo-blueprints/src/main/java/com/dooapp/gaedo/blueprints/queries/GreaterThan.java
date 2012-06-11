package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.blueprints.BluePrintsBackedFinderService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class GreaterThan<ComparableType extends Comparable<ComparableType>> extends ComparableValuedVertexTest<ComparableType> implements VertexTest {

	public GreaterThan(ServiceRepository repository, Iterable<Property> p, ComparableType expected, boolean strictly) {
		super(repository, p, expected, strictly);
	}

	@Override
	protected boolean doCompare(ComparableType effective) {
		return strictly ? effective.compareTo(expected)>0 : effective.compareTo(expected)>=0;
	}
}
