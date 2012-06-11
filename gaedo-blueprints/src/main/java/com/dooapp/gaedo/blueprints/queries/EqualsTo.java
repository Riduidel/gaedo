package com.dooapp.gaedo.blueprints.queries;

import com.dooapp.gaedo.blueprints.BluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class EqualsTo extends MonovaluedValuedVertexTest<Object> implements VertexTest {
	public EqualsTo(ServiceRepository repository, Iterable<Property> p, Object value) {
		super(repository, p, value);
	}

	@Override
	protected boolean matchesLiteral(Object effective) {
		// handle specifically the case of numbers to allow easier rounding
		if (expected instanceof Number && effective instanceof Number) {
			Number expectedNumber = (Number) expected;
			Number effectiveNumber = (Number) effective;
			double expectedDouble = expectedNumber.doubleValue();
			double effectiveDouble = effectiveNumber.doubleValue();
			// Number comparison is always a matter of making sure they're both
			// below an epsilon. Here, i use a relative (but expected) good
			// epsilon
			return Math.abs(expectedDouble - effectiveDouble) < (expectedDouble / 100000);
		} else {
			return effective.equals(expected);
		}
	}

	/**
	 * This one is quite simple : load object, then call equals on it
	 * 
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.MonovaluedValuedVertexTest#callMatchManaged(com.tinkerpop.blueprints.pgm.Vertex,
	 *      com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		// First check if id is the same
		BluePrintsBackedFinderService service = getService();
		return isVertexEqualsTo(currentVertex, service, expected, false);
	}

}
