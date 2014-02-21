package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public class ContainsString extends AbstractStringVertexTest<String> implements VertexTest {

	public ContainsString(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, String value) {
		super(strategy, driver, p, value);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean testString(String searched, String expected) {
		return searched.contains(expected);
	}

}
