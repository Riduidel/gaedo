package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

/**
 * Test a vertex accessible by a given path has a given value.
 * @author ndx
 *
 */
public class VertexPropertyTest extends TargettedVertexTest implements VertexTest {

	private final String propertyName;

	private final Object expected;

	public VertexPropertyTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> path, String propertyName, Object expected) {
		super(strategy, driver, path);
		this.propertyName = propertyName;
		this.expected = expected;
	}

	public boolean matches(Vertex examined) {
		Object vertexValue = examined.getProperty(propertyName);
		return (expected==null && vertexValue==null) || (expected!=null && expected.equals(vertexValue));
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @return the propertyName
	 * @category getter
	 * @category propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @return the expected
	 * @category getter
	 * @category expected
	 */
	public Object getExpected() {
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
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
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
		VertexPropertyTest other = (VertexPropertyTest) obj;
		if (expected == null) {
			if (other.expected != null)
				return false;
		} else if (!expected.equals(other.expected))
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}
}
