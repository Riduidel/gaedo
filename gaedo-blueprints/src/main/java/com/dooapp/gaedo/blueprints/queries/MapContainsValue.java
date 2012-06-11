package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class MapContainsValue extends MonovaluedValuedVertexTest<Object> implements VertexTest {

	public MapContainsValue(ServiceRepository repository, Iterable<Property> p, Object value) {
		super(repository, p, value);
	}

	@Override
	protected boolean matchesLiteral(Object effective) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+MonovaluedValuedVertexTest.class.getName()+"#matches has not yet been implemented AT ALL");
	}

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+MonovaluedValuedVertexTest.class.getName()+"#callMatchManaged has not yet been implemented AT ALL");
	}

}
