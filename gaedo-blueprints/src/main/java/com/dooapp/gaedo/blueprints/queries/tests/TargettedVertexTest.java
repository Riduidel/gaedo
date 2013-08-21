package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Iterator;
import java.util.TreeMap;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

/**
 * Base class for all vertex tests
 * @author ndx
 *
 */
public class TargettedVertexTest {

	/**
	 * Check if vertex is the one associated to the given object
	 * @param currentVertex examined vertex
	 * @param service service used to get stored object value
	 * @param expected expected object value
	 * @param deepInspect should we perform deep object inspection to check equaality ? This requires object loading and is as a consequence by far slower
	 * @return true if object is equals to vertex associated object
	 */
	protected static boolean isVertexEqualsTo(Vertex currentVertex, AbstractBluePrintsBackedFinderService service, Object expected, boolean deepInspect, ObjectCache objectsBeingAccessed) {
		GraphDatabaseDriver driver = service.getDriver();
		Object expectedId = service.getIdOf(expected);
		if (expectedId.equals(driver.getIdOf(currentVertex))) {
			return true;
		} else if(deepInspect) {
			Object value = service.loadObject(currentVertex, objectsBeingAccessed);
			return ((expected == null && value == null) || (expected != null && expected.equals(value)));
		} else {
			return false;
		}
	}

	protected ObjectCache objectsBeingAccessed = ObjectCache.create(CascadeType.REFRESH);

	/**
	 * Path used to navigate from source node to inspected one
	 */
	protected final Iterable<Property> path;
	/**
	 * Used path length, useful for checking that a null value can be found
	 */
	protected final int pathLength;
	/**
	 * Lazy loaded end of path property
	 */
	private transient Property endProperty;
	/**
	 * Driver used to start this query
	 */
	private final GraphDatabaseDriver driver;
	/**
	 * Strategy used to navigate the edges
	 */
	protected final GraphMappingStrategy<?> strategy;

	/**
	 * Builds the vertex test
	 * @param strategy graph mapping strategy, used to define which eddges will be navigated
	 * @param driver database driver to perform common rehydration operations
	 * @param path property path to navigate to that particular test.
	 */
	public TargettedVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> path) {
		this.path = path;
		this.pathLength = computeLengthOf(path);
		this.driver = driver;
		this.strategy = strategy;
	}

	private int computeLengthOf(Iterable<Property> path) {
		int returned = 0;
		if(path!=null) {
			for(Property p : path) {
				returned++;
			}
		}
		return returned;
	}

	/**
	 * @return the path
	 * @category getter
	 * @category path
	 */
	public Iterable<Property> getPath() {
		return path;
	}

	/**
	 * @return the repository
	 * @category getter
	 * @category repository
	 */
	public ServiceRepository getRepository() {
		return driver.getRepository();
	}

	/**
	 * Source driver of this query
	 * @return a driver
	 */
	public GraphDatabaseDriver getDriver() {
		return driver;
	}

	protected StringBuilder toString(int deepness, StringBuilder builder) {
		for(int i=0;i<deepness;i++) {
			builder.append("\t");
		}
		if(path!=null) {
			Iterator<Property> pathIter = path.iterator();
			while (pathIter.hasNext()) {
				Property property = (Property) pathIter.next();
				builder.append(property.getName());
				if(pathIter.hasNext()) {
					builder.append(".");
				}
			}
		}
		builder.append(" ").append(getClass().getSimpleName()).append(" ");
		return builder;
	}

	public String toString() {
		return toString(0, new StringBuilder()).toString();
	}

	protected Property getEndProperty() {
		if(endProperty==null) {
			for(Property p : path) {
				endProperty = p;
			}
		}
		return endProperty;
	}
}
