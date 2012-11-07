package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Iterator;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class Anything extends TargettedVertexTest implements VertexTest {

	public Anything(GraphDatabaseDriver driver, Iterable<Property> p) {
		super(driver, p);
	}

	/**
	 * Simply ensure there is an edge going
	 * @param examined
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTest#matches(com.tinkerpop.blueprints.pgm.Vertex)
	 */
	@Override
	public boolean matches(Vertex examined) {
		// Navigates to the first target edge and perform test when reached
		Vertex currentVertex = examined;
		for(Property currentProperty : path) {
			Iterator<Edge> edges = currentVertex.getOutEdges(GraphUtils.getEdgeNameFor(currentProperty)).iterator();
			if(edges.hasNext()) {
				currentVertex = edges.next().getInVertex();
			} else {
				return false;
			}
		}
		return (currentVertex!=null);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

}
