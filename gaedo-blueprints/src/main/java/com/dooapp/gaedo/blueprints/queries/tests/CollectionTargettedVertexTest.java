package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.LinkedList;
import java.util.List;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Base class for collection tests
 * @author ndx
 *
 */
public abstract class CollectionTargettedVertexTest extends TargettedVertexTest implements VertexTest {

	public CollectionTargettedVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> path) {
		super(strategy, driver, path);
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
		List<Property> usedPath = CollectionUtils.asList(path);
		return matchesCollection(examined, usedPath, null);
	}


	/**
	 * Recursively navigate elements of the property path to the final property.
	 * Notice it's the responsibility iof subclasses to say if evaluation should continue
	 * @param examined
	 * @param path
	 * @param last last navigated property, given for evaluation
	 * @return
	 */
	private boolean matchesCollection(Vertex examined, List<Property> path, Property last) {
		boolean returned = getInitialReturned();
		if(path.size()==0) {
			// we've reached the end
			return matchesVertex(examined, last);
		} else {
			Property evaluated = path.get(0);
			List<Property> remaining =  path.size()>1 ? path.subList(1, path.size()) : new LinkedList<Property>();
			Iterable<Edge> edges = strategy.getOutEdgesFor(examined, evaluated);
			for(Edge e : edges) {
				returned = combineReturnedWith(matchesCollection(e.getVertex(Direction.IN), remaining, evaluated), returned);
			}
			// Also test if property uses on literal value (but this only can have meaning if we're at the last property on path,
			// that's to say when remaining path length is zero)
			if(remaining.size()==0) {
				returned = combineReturnedWith(matchesCollection(examined, remaining, evaluated), returned);
			}
		}
		return returned;
	}


	/**
	 * Combine previously evaluated returned value with current one
	 * @param current current evaluation (can be the direct result of {@link #matchesVertex(Vertex, Property)})
	 * @param previousReturned previous result (can be {@link #getInitialReturned()})
	 * @return
	 */
	protected abstract boolean combineReturnedWith(boolean current, boolean previousReturned);


	protected abstract boolean getInitialReturned();


	/**
	 * Effectively matches a vertex with conditions defined in test
	 * @param examined examined vertex
	 * @param property property used to evaluate object and vertex
	 * @return true if vertex matched, false otherwise
	 */
	protected abstract boolean matchesVertex(Vertex examined, Property property);
}
