package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class OrVertexTest extends AggregatedTargettedVertexTest implements VertexTest {

	public OrVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p) {
		super(strategy, driver, p);
	}

	@Override
	public boolean matches(Vertex examined) {
		boolean returned = false;
		for(VertexTest v : tests) {
			if(!returned) {
				returned |= v.matches(examined);
			}
		}
		return returned;
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
