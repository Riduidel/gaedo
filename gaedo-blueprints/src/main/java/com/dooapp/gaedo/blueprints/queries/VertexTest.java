package com.dooapp.gaedo.blueprints.queries;

import com.tinkerpop.blueprints.pgm.Vertex;

public interface VertexTest {

	/**
	 * Check if provided vertex match this test requirements
	 * @param examined any vertex
	 * @return tyrue if vertex match, false otherwise (awesome, dude)
	 */
	boolean matches(Vertex examined);

}
