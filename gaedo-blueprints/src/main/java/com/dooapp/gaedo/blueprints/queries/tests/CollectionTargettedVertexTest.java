package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Base class for collection tests
 * @author ndx
 *
 */
public abstract class CollectionTargettedVertexTest extends TargettedVertexTest implements VertexTest {

	public CollectionTargettedVertexTest(ServiceRepository repository, Iterable<Property> path) {
		super(repository, path);
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
			// we've reeached the end
			return matchesVertex(examined, last);
		} else {
			Property evaluated = path.get(0);
			List<Property> remaining =  path.size()>1 ? path.subList(1, path.size()) : new LinkedList<Property>();
			Iterable<Edge> edges = examined.getOutEdges(GraphUtils.getEdgeNameFor(evaluated));
			for(Edge e : edges) {
				returned = combineReturnedWith(matchesCollection(e.getInVertex(), remaining, evaluated), returned);
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
