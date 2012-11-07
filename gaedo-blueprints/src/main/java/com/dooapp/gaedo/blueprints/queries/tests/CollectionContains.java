package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Iterator;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class CollectionContains extends CollectionTargettedVertexTest implements VertexTest {

	private final Object expected;

	public CollectionContains(GraphDatabaseDriver driver, Iterable<Property> p, Object value) {
		super(driver, p);
		this.expected = value;
	}

	@Override
	protected boolean combineReturnedWith(boolean current, boolean previousReturned) {
		return current || previousReturned;
	}

	@Override
	protected boolean getInitialReturned() {
		return false;
	}

	@Override
	protected boolean matchesVertex(Vertex examined, Property property) {
		EqualsTo used = new EqualsTo(getDriver(), path, expected);
		return used.matchesVertex(examined, property);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the value
	 * @category getter
	 * @category value
	 */
	public Object getExpected() {
		return expected;
	}

	@Override
	protected StringBuilder toString(int deepness, StringBuilder builder) {
		StringBuilder returned = super.toString(deepness, builder);
		returned.append(getExpected());
		return returned;
	}
	
	public Object getExpectedAsValue() {
		return MonovaluedValuedVertexTest.getExpectedAsValueOf(getEndProperty(), getExpected());
	}
}
