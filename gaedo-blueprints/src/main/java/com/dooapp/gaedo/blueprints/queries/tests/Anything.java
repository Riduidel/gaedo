package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Iterator;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class Anything extends MonovaluedValuedVertexTest<Object> implements VertexTest {

	public Anything(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p) {
		super(strategy, driver, p, "" /* we define no value for expected value, as we won't match it "directly". But we also don't use null, which is unfortunatly a special case */);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * If there is a vertex for final property, then simply return true
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.MonovaluedValuedVertexTest#callMatchManaged(com.tinkerpop.blueprints.Vertex, com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		return true;
	}

	/**
	 * We return true if vertex has a property named after the final property
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.MonovaluedValuedVertexTest#callMatchLiteral(com.tinkerpop.blueprints.Vertex, com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		return currentVertex.getPropertyKeys().contains(GraphUtils.getEdgeNameFor(finalProperty));
	}

	/**
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.MonovaluedValuedVertexTest#shouldMatchManagedVertex(com.tinkerpop.blueprints.Vertex, com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean shouldMatchManagedVertex(Vertex currentVertex, Property finalProperty) {
		return getRepository().containsKey(finalProperty.getType());
	}

}
