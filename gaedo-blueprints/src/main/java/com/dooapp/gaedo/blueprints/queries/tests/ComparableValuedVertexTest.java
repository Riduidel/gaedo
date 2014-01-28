package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.PrimitiveUtils;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Vertex;

public abstract class ComparableValuedVertexTest<ComparableType extends Comparable<ComparableType>> extends MonovaluedValuedVertexTest<ComparableType> {

	/**
	 * Comparison utility method, ensuring no comaprison of number can drop a ClassCastException
	 * @param effective
	 * @param expected
	 * @return their comparison ... what did you expect ?
	 */
	protected static <ComparableType extends Comparable<ComparableType>> int compareCasted(ComparableType effective, ComparableType expected) {
		Class expectedClass = Utils.maybeObjectify(expected.getClass());
		if(Number.class.isAssignableFrom(expectedClass)) {
			Double effectiveDouble = PrimitiveUtils.as((Number) effective, Double.class);
			Double expectedDouble = PrimitiveUtils.as((Number) expected, Double.class);
			return effectiveDouble.compareTo(expectedDouble);
		} else {
			// nothing can be done for non number classes
			return -1*expected.compareTo(effective);
		}
	}


	/**
	 * When true, comparison is strict
	 */
	protected final boolean strictly;

	public ComparableValuedVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, ComparableType value, boolean strictly) {
		super(strategy, driver, p, value);
		this.strictly = strictly;
	}

	protected abstract boolean doCompare(ComparableType effective);

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		AbstractBluePrintsBackedFinderService service = getService();
		Object value = service.loadObject(currentVertex, objectsBeingAccessed);
		return doCompare((ComparableType) value);
	}


	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		Class<?> valueClass = finalProperty.getType();
		LiteralTransformer literalTransformer = Literals.get(valueClass);
		String effectiveGraphValue = currentVertex.getProperty(GraphUtils.getEdgeNameFor(finalProperty));
		ClassLoader classLoader = expected.getClass().getClassLoader();
		ComparableType value = (ComparableType) literalTransformer.fromString(effectiveGraphValue, valueClass, classLoader, objectsBeingAccessed);
		return doCompare(value);
	}
}
