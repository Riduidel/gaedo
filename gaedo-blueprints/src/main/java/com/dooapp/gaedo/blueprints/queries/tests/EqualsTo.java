package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class EqualsTo extends MonovaluedValuedVertexTest<Object> implements VertexTest {
	public EqualsTo(ServiceRepository repository, Iterable<Property> p, Object value) {
		super(repository, p, value);
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
		IndexableGraphBackedFinderService service = getService();
		return isVertexEqualsTo(currentVertex, service, expected, false);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		LiteralTransformer used = Literals.get(finalProperty.getGenericType());
		if(used==null) {
			throw new UnsupportedOperationException(finalProperty+" doesn't seems to store literal value. How could we check its value then ?");
		} else {
			return used.isVertexEqualsTo(currentVertex, getExpectedAsValue());
		}
	}

}
