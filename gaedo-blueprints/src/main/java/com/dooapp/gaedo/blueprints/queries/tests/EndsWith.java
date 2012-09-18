package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class EndsWith extends MonovaluedValuedVertexTest<String> implements VertexTest {

	public EndsWith(GraphDatabaseDriver driver, Iterable<Property> p, String value) {
		super(driver, p, value);
	}

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
		return value.endsWith(getExpected());
	}

}
