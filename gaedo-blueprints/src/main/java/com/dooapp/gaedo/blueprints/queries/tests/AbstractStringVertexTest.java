package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public abstract class AbstractStringVertexTest<ExpectedType> extends MonovaluedValuedVertexTest<String, ExpectedType> {

	public AbstractStringVertexTest(GraphMappingStrategy strategy, GraphDatabaseDriver driver, Iterable<Property> p, ExpectedType value) {
		super(strategy, driver, p, value);
	}

	/**
	 * As {@link #matchesLiteral(String)} ensures value is a string by force-calling toString() on it, we can
	 * take the {@link #callMatchLiteral(Vertex, Property)} path
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see #callMatchLiteral(Vertex, Property)
	 * @see com.dooapp.gaedo.blueprints.queries.tests.MonovaluedValuedVertexTest#callMatchManaged(com.tinkerpop.blueprints.pgm.Vertex, com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		return callMatchLiteral(currentVertex, finalProperty);
	}

	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		String value = getLiteralValue(currentVertex, finalProperty);
		return testString(value, expected);
	}

	protected abstract boolean testString(String searched, ExpectedType expected);
}