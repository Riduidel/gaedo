package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class ContainsString extends MonovaluedValuedVertexTest<String> implements VertexTest {

	public ContainsString(GraphDatabaseDriver driver, Iterable<Property> p, String value) {
		super(driver, p, value);
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
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		String value = (String) Literals.get(String.class).loadObject(getDriver(), currentVertex);
		return value.contains(getExpected());
	}

}
