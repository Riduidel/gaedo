package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class EndsWith extends MonovaluedValuedVertexTest<String> implements VertexTest {

	public EndsWith(ServiceRepository repository, Iterable<Property> p, String value) {
		super(repository, p, value);
	}

	@Override
	protected boolean matchesLiteral(String effective) {
		if(expected==null)
			return true;
		return effective.endsWith(expected.toString());
	}

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		return callMatchLiteral(currentVertex, finalProperty);
	}

}
