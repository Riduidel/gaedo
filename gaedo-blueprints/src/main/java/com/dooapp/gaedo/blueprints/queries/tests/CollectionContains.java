package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.operations.LiteralInCollectionUpdaterProperty;
import com.dooapp.gaedo.blueprints.operations.Updater;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public class CollectionContains extends CollectionTargettedVertexTest implements VertexTest {

	private final Object expected;

	public CollectionContains(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, Object value) {
		super(strategy, driver, p);
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

	/**
	 * @param examined
	 * @param property
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.CollectionTargettedVertexTest#matchesVertex(com.tinkerpop.blueprints.Vertex, com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean matchesVertex(Vertex examined, Property property) {
		if(Literals.containsKey(expected.getClass())) {
			return callMatchLiteral(examined, property);
		} else {
			return callMatchManaged(examined, property);
		}
	}

	/**
	 * Matching a vertex is rather complicated when expected value is a literal one, as literals are stored as vertex properties
	 * So we have to use the inverted indexing property to see if it is in collection
	 * @param examined
	 * @param property
	 * @return
	 */
	private boolean callMatchLiteral(Vertex examined, Property property) {
		EqualsTo used = new EqualsTo(strategy, getDriver(), path, Updater.ELEMENT_IN_COLLECTION_MARKER);
		return used.matchesVertex(examined, new LiteralInCollectionUpdaterProperty(property, expected, Updater.ELEMENT_IN_COLLECTION_MARKER));
	}

	private boolean callMatchManaged(Vertex examined, Property property) {
		EqualsTo used = new EqualsTo(strategy, getDriver(), path, expected);
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
