package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.EqualsTo;
import com.dooapp.gaedo.blueprints.queries.tests.MapContainsKey;
import com.dooapp.gaedo.blueprints.queries.tests.MapContainsValue;
import com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitor;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Class visiting vertex test to grab possible 
 * @author ndx
 *
 */
public class VertexRootsCollector extends VertexTestVisitorAdapter implements VertexTestVisitor {
	/**
	 * We use a {@link LinkedHashMap} to keep test ordering, as it allows us to avoid loading all object values (usuall the class test will be set as last one)
	 */
	private Map<Vertex, Iterable<Property>> result = new LinkedHashMap<Vertex, Iterable<Property>>();
	
	private final IndexableGraphBackedFinderService<?, ?> service;

	/**
	 * Cache of objects being loaded during roots collection building
	 */
	private transient Map<String, Object> objectsBeingAccessed = new TreeMap<String, Object>();

	public VertexRootsCollector(IndexableGraphBackedFinderService<?, ?> service) {
		super();
		this.service = service;
	}

	public Map<Vertex, Iterable<Property>> getResult() {
		return result;
	}

	/**
	 * Not queries are NEVER visited
	 * @param notVertexTest
	 * @return false
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#startVisit(com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest)
	 */
	@Override
	public boolean startVisit(NotVertexTest notVertexTest) {
		return false;
	}
	
	/**
	 * We should of course support OR queries, but for now we don't, as it implies concatenating result lists and having a very specific behaviour during the search for the best matching vertex
	 * @param orVertexTest
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#startVisit(com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest)
	 */
	@Override
	public boolean startVisit(OrVertexTest orVertexTest) {
		return false;
	}
	
	@Override
	public void visit(CollectionContains collectionContains) {
		result.put(load(collectionContains.getExpectedAsValue()), collectionContains.getPath());
	}

	private Vertex load(Object expected) {
		return service.getVertexFor(expected, CascadeType.REFRESH, objectsBeingAccessed);
	}
	
	@Override
	public void visit(EqualsTo equalsTo) {
		result.put(load(equalsTo.getExpectedAsValue()), equalsTo.getPath());
	}
	
	@Override
	public void visit(MapContainsKey mapContainsKey) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+VertexRootsCollector.class.getName()+"#visit has not yet been implemented AT ALL");
	}
	
	@Override
	public void visit(MapContainsValue mapContainsValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+VertexRootsCollector.class.getName()+"#visit has not yet been implemented AT ALL");
	}
}
