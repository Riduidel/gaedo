package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.indexable.IndexNames;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.EqualsTo;
import com.dooapp.gaedo.blueprints.queries.tests.MapContainsKey;
import com.dooapp.gaedo.blueprints.queries.tests.MapContainsValue;
import com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexPropertyTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitor;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Class visiting vertex test to grab possible query roots. What are query roots ? 
 * Well, they are the vertices a query can use to navigate the graph and find matching nodes.
 * Suppose, as a canonical example, we want to find the following objects
 * <pre>
 * AND
 *	  Posts.note ==? 4.0
 *	  Posts.author.login ==? "user login"
 *	  Object.classes contains Post.class
 * </pre>
 *
 * It is obvious we can start by navigating the class vertex (the one holding the Post.class value) and scan all objects linked to that 
 * class through the Object.classes relationship. But, is it the optimal path ? Maybe we have only two or three articles written by that given author ...
 * and only one having as note 4.0.
 * As a consequence, this class tries to give some answer elements by visiting the query, and providing a map linking root vertices to 
 * the properties path used to find them.
 * 
 * Fo the given query, and after having visited the object containing that query, this method will as a consequence return, through the {@link #getResult()}
 * method call, a map linking one vertex to each navigable query predicate.
 * For now, the usable predicates are
 * <ul>
 * <li>CollectionContains</li>
 * <li>EqualsTo</li>
 * </ul>
 * Notice that OR and NOT query combators are NOT navigated
 * @author ndx
 *
 */
public class VertexRootsCollector extends VertexTestVisitorAdapter implements VertexTestVisitor {
	/**
	 * We use a {@link LinkedHashMap} to keep test ordering, as it allows us to avoid loading all object values (usuall the class test will be set as last one)
	 */
	private Map<Iterable<Vertex>, Iterable<Property>> result = new LinkedHashMap<Iterable<Vertex>, Iterable<Property>>();
	
	private final AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service;

	/**
	 * Cache of objects being loaded during roots collection building
	 */
	private transient Map<String, Object> objectsBeingAccessed = new TreeMap<String, Object>();

	public VertexRootsCollector(AbstractBluePrintsBackedFinderService<?, ?, ?> service) {
		super();
		this.service = service;
	}

	public Map<Iterable<Vertex>, Iterable<Property>> getResult() {
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

	/**
	 * Load if possible vertex associated to entry
	 * @param expected
	 * @return a list containing that vertex if it existed, or null elsewhere.
	 */
	private Collection<Vertex> load(Object expected) {
		Vertex vertexFor = service.getVertexFor(expected, CascadeType.REFRESH, objectsBeingAccessed);
		if(vertexFor==null)
			return Collections.emptyList();
		else
			return Arrays.asList(vertexFor);
	}
	
	@Override
	public void visit(EqualsTo equalsTo) {
		if(equalsTo.getExpected()!=null)
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
	
	/**
	 * Add all vertices with the given path
	 * @param vertexPropertyTest
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#visit(com.dooapp.gaedo.blueprints.queries.tests.VertexPropertyTest)
	 */
	@Override
	public void visit(final VertexPropertyTest vertexPropertyTest) {
		// TODO improve that code !!!
		Graph g = service.getDatabase();
		if (g instanceof IndexableGraph) {
			final IndexableGraph indexable = (IndexableGraph) g;
			final Index<Vertex> vertices = indexable
							.getIndex(IndexNames.VERTICES.getIndexName(), Vertex.class);
			result.put(new Iterable<Vertex>() {
				
				@Override
				public Iterator<Vertex> iterator() {
					Iterable<Vertex> matching = vertices.get(vertexPropertyTest.getPropertyName(), vertexPropertyTest.getExpected());
					return matching.iterator();
				}
			}, vertexPropertyTest.getPath());
		} else {
			throw new UnsupportedOperationException("not yet implemented");
		}
	}
}
