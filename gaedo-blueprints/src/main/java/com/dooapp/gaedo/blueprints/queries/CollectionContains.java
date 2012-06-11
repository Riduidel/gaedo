package com.dooapp.gaedo.blueprints.queries;

import java.util.Iterator;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class CollectionContains extends CollectionTargettedVertexTest implements VertexTest {

	private Object value;

	public CollectionContains(ServiceRepository repository, Iterable<Property> p, Object value) {
		super(repository, p);
		this.value = value;
	}

	@Override
	protected boolean combineReturnedWith(boolean current, boolean previousReturned) {
		return current || previousReturned;
	}

	@Override
	protected boolean getInitialReturned() {
		return false;
	}

	@Override
	protected boolean matchesVertex(Vertex examined, Property property) {
		EqualsTo used = new EqualsTo(repository, path, value);
		return used.matchesVertex(examined, property);
	}

}
