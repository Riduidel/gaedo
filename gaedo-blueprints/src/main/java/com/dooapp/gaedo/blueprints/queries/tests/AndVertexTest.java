package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class AndVertexTest extends AggregatedTargettedVertexTest implements CompoundVertexTest {
	public AndVertexTest(GraphDatabaseDriver driver, Iterable<Property> p) {
		super(driver, p);
	}

	@Override
	public boolean matches(Vertex examined) {
		boolean returned = true;
		for(VertexTest v : tests) {
			if(returned) {
				returned &= v.matches(examined);
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
