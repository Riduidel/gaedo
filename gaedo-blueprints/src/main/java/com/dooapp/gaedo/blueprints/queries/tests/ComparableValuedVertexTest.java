package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.TreeMap;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public abstract class ComparableValuedVertexTest<ComparableType extends Comparable<ComparableType>> extends MonovaluedValuedVertexTest<ComparableType> {

	/**
	 * When true, comparison is strict
	 */
	protected final boolean strictly;

	public ComparableValuedVertexTest(GraphDatabaseDriver driver, Iterable<Property> p, ComparableType value, boolean strictly) {
		super(driver, p, value);
		this.strictly = strictly;
	}

	protected abstract boolean doCompare(ComparableType effective);

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		AbstractBluePrintsBackedFinderService service = getService();
		Object value = service.loadObject(currentVertex, new TreeMap<String, Object>());
		return doCompare((ComparableType) value);
	}


	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		ComparableType value = (ComparableType) Literals.get(finalProperty.getType()).loadObject(getDriver(), currentVertex);
		return doCompare(value);
	}
}
