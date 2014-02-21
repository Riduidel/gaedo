package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public class EqualsTo extends MonovaluedValuedVertexTest<Object, Object> implements VertexTest {
	public EqualsTo(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, Object value) {
		super(strategy, driver, p, value);
	}

	/**
	 * This one is quite simple : load object, then call equals on it
	 *
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.MonovaluedValuedVertexTest#callMatchManaged(com.tinkerpop.blueprints.pgm.Vertex,
	 *      com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		// First check if id is the same
		return isVertexEqualsTo(currentVertex, getService(), driver, expected, false, objectsBeingAccessed);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		LiteralTransformer used = Literals.get(finalProperty.getGenericType());
		return testOnVertex(expected, currentVertex, finalProperty, used);
	}

	public static boolean testOnVertex(Object expected, Vertex currentVertex, Property finalProperty, LiteralTransformer used) {
		if(used==null) {
			throw new UnsupportedOperationException(finalProperty+" doesn't seems to store literal value. How could we check its value then ?");
		} else {
			if (expected == null) {
				return currentVertex == null || currentVertex.getProperty(GraphUtils.getEdgeNameFor(finalProperty)) == null;
			} else {
				if (currentVertex == null) {
					return false;
				} else {
					String effectiveGraphValue = currentVertex.getProperty(GraphUtils.getEdgeNameFor(finalProperty));
					return used.areEquals(expected, effectiveGraphValue);
				}
			}
		}
	}
}
