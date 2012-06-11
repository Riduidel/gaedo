package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * A not is just the opposite of an or : if or returns true, not return false
 * @author ndx
 *
 */
public class NotVertexTest extends AndVertexTest implements VertexTest, CompoundVertexTest {

	public NotVertexTest(ServiceRepository repository, Iterable<Property> p) {
		super(repository, p);
	}

	@Override
	public boolean matches(Vertex examined) {
		return !super.matches(examined);
	}
}
