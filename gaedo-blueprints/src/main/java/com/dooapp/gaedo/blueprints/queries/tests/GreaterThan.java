package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;

public class GreaterThan<ComparableType extends Comparable<ComparableType>> extends ComparableValuedVertexTest<ComparableType> implements VertexTest {

	public GreaterThan(ServiceRepository repository, Iterable<Property> p, ComparableType expected, boolean strictly) {
		super(repository, p, expected, strictly);
	}

	@Override
	protected boolean doCompare(ComparableType effective) {
		return strictly ? effective.compareTo(expected)>0 : effective.compareTo(expected)>=0;
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}
}
