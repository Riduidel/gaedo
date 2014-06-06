package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.EmptyStackException;
import java.util.Iterator;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.operations.Loader;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.utils.VertexPathNavigator;
import com.dooapp.gaedo.blueprints.utils.VertexPathNavigator.VertexLocation;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.TypeProperty;
import com.dooapp.gaedo.utils.PrimitiveUtils;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Base class for all simple tests (like contains, greater than, ...). Notice
 * {@link #expected} is used for both direct value comparison (for literals) and
 * id check (for managed objects). This is done by asking the
 * {@link TargettedVertexTest#serviceRrepository} if {@link #expected} is a
 * managed value
 *
 * @author ndx
 *
 * @param <ValueType>
 *            current value type
 */
public abstract class MonovaluedValuedVertexTest<ValueType extends Object, ExpectedType extends Object> extends TargettedVertexTest implements VertexTest {

	/**
	 * Expected value
	 */
	protected final ExpectedType expected;

	public MonovaluedValuedVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p, ExpectedType value) {
		super(strategy, driver, p);
		this.expected = value;
	}

	/**
	 * To match node
	 *
	 * @param examined
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTest#matches(com.tinkerpop.blueprints.pgm.Vertex)
	 */
	@Override
	public boolean matches(Vertex examined) {
		if (!path.iterator().hasNext()) {
			// path may be empty when vertex is directly examined, typically for an instanceof.
			// in such a case, we can directly call the matchesVertex WITH A NULL PROPERTY
			return matchesVertex(examined, null);
		} else {
			// Navigates to the first target edge and perform test when reached
			VertexPathNavigator navigator = new VertexPathNavigator(strategy, driver, examined);
			VertexLocation destination = navigator.navigateOn(path);
			// null final property indicates object has no value for that
			// property
			if (!destination.isNavigationSuccessfull()) {
				return matchesNull();
			} else {
				return matchesVertex(destination.vertex(), destination.property());
			}
		}
	}

	/**
	 * Define what that test must do when encountering a null value.
	 *
	 * @return true if expected is null, false elsewhere.
	 */
	protected boolean matchesNull() {
		return expected == null;
	}

	/**
	 * Perform the final vertex match
	 *
	 * @param currentVertex
	 * @param finalProperty
	 *            property used to access value. It can be null if, as an
	 *            example, currentVertex is initial vertex (typically when doing
	 *            an instanceOf or equalsTo test)
	 * @return
	 */
	public boolean matchesVertex(Vertex currentVertex, Property finalProperty) {
		if (expected == null) {
			return currentVertex == null;
		} else {
			if (shouldMatchManagedVertex(currentVertex, finalProperty)) {
				return callMatchManaged(currentVertex, finalProperty);
			} else {
				return callMatchLiteral(currentVertex, finalProperty);
			}
		}
	}

	/**
	 * Check if match should be made for a literal value or a managed value.
	 *
	 * @param currentVertex
	 *            currently evaluated verted
	 * @param finalProperty
	 *            property on which match should be performed
	 * @return
	 */
	protected boolean shouldMatchManagedVertex(Vertex currentVertex, Property finalProperty) {
		if(finalProperty==null) {
			// test on input vertex
			return currentVertex.getProperty(GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE))!=null;
		} else {
			return getRepository().containsKey(expected.getClass());
		}
	}

	/**
	 * Check vertex corresponding to given final property matches with a managed
	 * object (that's to say an object for which exist a
	 * {@link IndexableGraphBackedFinderService}
	 *
	 * @param currentVertex
	 *            vertex corresponding to finalProperty in property path
	 * @param finalProperty
	 *            property giving infos on class to use to read vertex value
	 *            (when needed)
	 * @return true if managed value matches ... yup, really awesome
	 */
	protected abstract boolean callMatchManaged(Vertex currentVertex, Property finalProperty);

	/**
	 * Check if literal (or tuple) value matches. Notice expected may in that
	 * case be a null value.
	 *
	 * @param currentVertex
	 *            vertex corresponding to finalProperty in property path
	 * @param finalProperty
	 *            property giving infos on class to use to read vertex value
	 *            (when needed)
	 * @return true if literal value matches ... yup, really awesome
	 */
	protected abstract boolean callMatchLiteral(Vertex currentVertex, Property finalProperty);

	/**
	 * Obtain service associated to expected value. BEWARE ! This service
	 * corresponds to {@link #expected}, which may be a literal (in which case
	 * obtained service will only be an exception). Usage iof this method should
	 * be restricted to cases where user is sure value is a serviceable one.
	 *
	 * @return
	 */
	protected AbstractBluePrintsBackedFinderService getService() {
		return (AbstractBluePrintsBackedFinderService) getRepository().get(expected.getClass());
	}

	public Object getExpectedAsValue() {
		return getExpectedAsValueOf(getEndProperty(), expected);
	}

	public static Object getExpectedAsValueOf(Property used, Object expected) {
		Class<?> usedType = Utils.maybeObjectify(expected == null ? Object.class : used.getType());
		Class<?> expectedValueClass = Utils.maybeObjectify(expected == null ? Object.class : expected.getClass());
		if (Number.class.isAssignableFrom(expectedValueClass) && Number.class.isAssignableFrom(usedType)) {
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

	/**
	 * Helper method transforming a vertex property into a literal. mainly
	 * useful for "basic" literals
	 *
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 */
	protected ValueType getLiteralValue(Vertex currentVertex, Property finalProperty) {
		Loader loader = new Loader();
		ClassLoader classLoader = expected == null ? expected.getClass().getClassLoader() : getClass().getClassLoader();
		return (ValueType) loader.loadSingleLiteral(classLoader, finalProperty, currentVertex, objectsBeingAccessed);
	}

	/**
	 * @return the expected
	 * @category getter
	 * @category expected
	 */
	public ExpectedType getExpected() {
		return expected;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expected == null) ? 0 : expected.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonovaluedValuedVertexTest other = (MonovaluedValuedVertexTest) obj;
		if (expected == null) {
			if (other.expected != null)
				return false;
		} else if (!expected.equals(other.expected))
			return false;
		return true;
	}
}
