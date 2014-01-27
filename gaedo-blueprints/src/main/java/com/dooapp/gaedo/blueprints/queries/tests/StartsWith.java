package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public class StartsWith extends MonovaluedValuedVertexTest<String> implements VertexTest {

	public StartsWith(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, String value) {
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
		LiteralTransformer used = Literals.get(finalProperty.getGenericType());
		String effectiveValue = currentVertex.getProperty(GraphUtils.getEdgeNameFor(finalProperty));
		String value = (String) Literals.get(String.class).fromString(effectiveValue, finalProperty.getType(), getClass().getClassLoader(), objectsBeingAccessed);
		return value.startsWith(getExpected());
	}
}
