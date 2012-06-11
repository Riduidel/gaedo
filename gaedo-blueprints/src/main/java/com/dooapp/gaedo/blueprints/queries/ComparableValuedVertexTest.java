package com.dooapp.gaedo.blueprints.queries;

import java.util.TreeMap;

import com.dooapp.gaedo.blueprints.BluePrintsBackedFinderService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public abstract class ComparableValuedVertexTest<ComparableType extends Comparable<ComparableType>> extends MonovaluedValuedVertexTest<ComparableType> {

	/**
	 * When true, comparison is strict
	 */
	protected final boolean strictly;

	public ComparableValuedVertexTest(ServiceRepository repository, Iterable<Property> p, ComparableType value, boolean strictly) {
		super(repository, p, value);
		this.strictly = strictly;
	}

	@Override
	protected boolean matchesLiteral(ComparableType effective) {
		return doCompare(effective);
	}

	protected abstract boolean doCompare(ComparableType effective);

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		BluePrintsBackedFinderService service = getService();
		Object value = service.loadObject(currentVertex, new TreeMap<String, Object>());
		return doCompare((ComparableType) value);
	}

}
