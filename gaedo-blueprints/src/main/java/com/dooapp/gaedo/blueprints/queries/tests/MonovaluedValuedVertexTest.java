package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Iterator;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.PrimitiveUtils;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Base class for all simple tests (like contains, greater than, ...).
 * Notice {@link #expected} is used for both direct value comparison (for literals) and id check (for managed objects).
 * This is done by asking the {@link TargettedVertexTest#serviceRrepository} if {@link #expected} is a managed value
 * 
 * @author ndx
 *
 * @param <ValueType> current value type
 */
public abstract class MonovaluedValuedVertexTest<ValueType extends Object> extends TargettedVertexTest implements VertexTest {

	/**
	 * Expected value
	 */
	protected final ValueType expected;

	public MonovaluedValuedVertexTest(GraphDatabaseDriver driver, Iterable<Property> p, ValueType value) {
		super(driver, p);
		if(value==null) {
			throw new NullExpectedValueException("impossible to build a "+getClass().getSimpleName()+" search condition on path "+p+" using null search value.");
		}
		this.expected = value;
	}

	/**
	 * To match node
	 * @param examined
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTest#matches(com.tinkerpop.blueprints.pgm.Vertex)
	 */
	@Override
	public boolean matches(Vertex examined) {
		// Navigates to the first target edge and perform etest when reached
		Vertex currentVertex = examined;
		Property finalProperty = null;
		// Counting path length allows us to check if we expect a null value
		int currentPathLength = 0;
		for(Property currentProperty : path) {
			Iterator<Edge> edges = currentVertex.getOutEdges(GraphUtils.getEdgeNameFor(currentProperty)).iterator();
			if(edges.hasNext()) {
				currentVertex = edges.next().getInVertex();
			} else {
				return false;
			}
			finalProperty = currentProperty;
			currentPathLength++;
		}
		if(finalProperty==null) {
			return false;
		} else {
			return matchesVertex(currentVertex, finalProperty);
		}
	}

	/**
	 * Perform the final vertex match
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 */
	public boolean matchesVertex(Vertex currentVertex, Property finalProperty) {
		if(getRepository().containsKey(expected.getClass())) {
			return callMatchManaged(currentVertex, finalProperty);
		} else {
			return callMatchLiteral(currentVertex, finalProperty);
		}
	}

	/**
	 * Check vertex corresponding to given final property matches with a managed object (that's to say an object 
	 * for which exist a {@link IndexableGraphBackedFinderService}
	 * @param currentVertex vertex corresponding to finalProperty in property path
	 * @param finalProperty property giving infos on class to use to read vertex value (when needed)
	 * @return true if managed value matches ... yup, really awesome
	 */
	protected abstract boolean callMatchManaged(Vertex currentVertex, Property finalProperty);

	/**
	 * Check if literal (or tuple) value matches
	 * @param currentVertex vertex corresponding to finalProperty in property path
	 * @param finalProperty property giving infos on class to use to read vertex value (when needed)
	 * @return true if literal value matches ... yup, really awesome
	 */
	protected abstract boolean callMatchLiteral(Vertex currentVertex, Property finalProperty);

	/**
	 * Obtain service associated to expected value. BEWARE ! This service corresponds to {@link #expected}, which may be a literal (in which case obtained service will only be an exception).
	 * Usage iof this method should be restricted to cases where user is sure value is a serviceable one.
	 * @return 
	 */
	protected AbstractBluePrintsBackedFinderService getService() {
		return (AbstractBluePrintsBackedFinderService) getRepository().get(expected.getClass());
	}

	/**
	 * @return the expected
	 * @category getter
	 * @category expected
	 */
	public ValueType getExpected() {
		return expected;
	}
	
	public Object getExpectedAsValue() {
		return getExpectedAsValueOf(getEndProperty(), getExpected());
	}

	public static Object getExpectedAsValueOf(Property used, Object expected) {
		Class<?> usedType = Utils.maybeObjectify(used.getType());
		Class<?> expectedValueClass = Utils.maybeObjectify(expected.getClass());
		if(Number.class.isAssignableFrom(expectedValueClass) && Number.class.isAssignableFrom(usedType)) {
			// same class cases are already handled with calls to maybeObjectify
			return PrimitiveUtils.as((Number) expected, (Class<? extends Number>) usedType);
		}
		return expected;
	}

	@Override
	protected StringBuilder toString(int deepness, StringBuilder builder) {
		StringBuilder returned = super.toString(deepness, builder);
		returned.append(expected).append("\n");
		return returned;
	}
}
