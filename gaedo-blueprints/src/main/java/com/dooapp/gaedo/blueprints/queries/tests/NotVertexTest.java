package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

/**
 * A not is just the opposite of an or : if or returns true, not return false
 * @author ndx
 *
 */
public class NotVertexTest extends AndVertexTest implements VertexTest, CompoundVertexTest {

	public NotVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p) {
		super(strategy, driver, p);
	}

	@Override
	public boolean matches(Vertex examined) {
		return !super.matches(examined);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		if(visitor.startVisit(this)) {
			for(VertexTest v : tests) {
				v.accept(visitor);
			}
		}
		visitor.endVisit(this);
	}
}
