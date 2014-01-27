package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.operations.Loader;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public class EndsWith extends MonovaluedValuedVertexTest<String> implements VertexTest {

	public EndsWith(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, String value) {
		super(strategy, driver, p, value);
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
		String value = getLiteralValue(currentVertex, finalProperty);
		return value.endsWith(getExpected());
	}

}
