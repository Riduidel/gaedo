package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class StartsWith extends MonovaluedValuedVertexTest<String> implements VertexTest {

	public StartsWith(ServiceRepository repository, Iterable<Property> p, String value) {
		super(repository, p, value);
	}

	@Override
	protected boolean matchesLiteral(String effective) {
		if(expected==null)
			return true;
		return effective.startsWith(expected.toString());
	}

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		return callMatchLiteral(currentVertex, finalProperty);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

}
