package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.patterns.Visitable;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public interface VertexTest extends Visitable<VertexTestVisitor> {

	/**
	 * Check if provided vertex match this test requirements
	 * @param examined any vertex
	 * @return tyrue if vertex match, false otherwise (awesome, dude)
	 */
	boolean matches(Vertex examined);


	/**
	 * @return the path
	 * @category getter
	 * @category path
	 */
	public Iterable<Property> getPath();

	/**
	 * @return the repository
	 * @category getter
	 * @category repository
	 */
	public GraphDatabaseDriver getDriver();
}
