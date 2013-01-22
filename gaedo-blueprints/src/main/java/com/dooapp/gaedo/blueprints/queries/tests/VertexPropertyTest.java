package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

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
}
