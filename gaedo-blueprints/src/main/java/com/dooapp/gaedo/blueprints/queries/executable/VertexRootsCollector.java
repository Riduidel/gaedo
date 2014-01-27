package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexNames;
import com.dooapp.gaedo.blueprints.operations.LiteralInCollectionUpdaterProperty;
import com.dooapp.gaedo.blueprints.operations.Updater;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.EqualsTo;
import com.dooapp.gaedo.blueprints.queries.tests.MapContainsKey;
import com.dooapp.gaedo.blueprints.queries.tests.MapContainsValue;
import com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexPropertyTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitor;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.WriteableKeyEntry;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
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

	private final AbstractBluePrintsBackedFinderService<? extends IndexableGraph, ?, ?> service;

	/**
	 * Cache of objects being loaded during roots collection building
	 */
	private transient ObjectCache objectsBeingAccessed = ObjectCache.create(CascadeType.REFRESH);

	public VertexRootsCollector(AbstractBluePrintsBackedFinderService<? extends IndexableGraph, ?, ?> service) {
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
		Entry<Iterable<Vertex>, Iterable<Property>> loadedEntry = load(collectionContains.getExpectedAsValue(), collectionContains.getPath());
		result.put(loadedEntry.getKey(), loadedEntry.getValue());
	}

	/**
	 * Load if possible vertex associated to entry
	 * @param expected
	 * @param path path by which vertex should be loaded. This path may be modified (especially because literals, being properties, change the path chain)
	 * @return a list containing that vertex if it existed, or null elsewhere.
	 */
	private <Type> Entry<Iterable<Vertex>, Iterable<Property>> load(Type expected, Iterable<Property> path) {
		// if value is a literal, well, it's time for an index lookup
		Class<Type> expectedClass = (Class<Type>) expected.getClass();
		if(expected!=null && Literals.containsKey(expectedClass)) {
			return loadVerticesWithLiteral(expected, path, expectedClass);
		} else {
			Vertex vertexFor = service.getVertexFor(expected, CascadeType.REFRESH, objectsBeingAccessed);
			if(vertexFor==null)
				return new WriteableKeyEntry<Iterable<Vertex>, Iterable<Property>>().withKey(new LinkedList<Vertex>()).withValue(new LinkedList<Property>());
			else
				return new WriteableKeyEntry<Iterable<Vertex>, Iterable<Property>>().withKey(Arrays.asList(vertexFor)).withValue(path);
		}
	}

	private <Type> Entry<Iterable<Vertex>, Iterable<Property>> loadVerticesWithLiteral(Type expected, Iterable<Property> path, Class<Type> expectedClass) {
		// Yup : gladly using implementation as it provides a last() method !
		LinkedList<Property> updatablePath = new LinkedList<Property>(CollectionUtils.asList(path));
		Property lastProperty = updatablePath.removeLast();
		LiteralTransformer<Type> transformer = Literals.get(expectedClass);
		String value = transformer.toString(expected);
		Index<Vertex> vertices = (Index<Vertex>) service.getDatabase().getIndex(IndexNames.VERTICES.getIndexName(), IndexNames.VERTICES.getIndexed());
		// stinky code fragment for collections : as each value is stored under a key in the form propertyName:index, we have to iterate upon them
		Set<Vertex> matchingVertices = new HashSet<Vertex>();
		if(Collection.class.isAssignableFrom(lastProperty.getType())) {
			String keyInIndex = GraphUtils.getEdgeNameFor(new LiteralInCollectionUpdaterProperty(lastProperty, expected, Updater.ELEMENT_IN_COLLECTION_MARKER));
			matchingVertices.addAll(CollectionUtils.asList(vertices.get(keyInIndex, Updater.ELEMENT_IN_COLLECTION_MARKER_GRAPH_VALUE)));
		} else {
			String singleEdgeNameFor = GraphUtils.getEdgeNameFor(lastProperty);
			matchingVertices.addAll(CollectionUtils.asList(vertices.get(singleEdgeNameFor, value)));
		}
		return new WriteableKeyEntry<Iterable<Vertex>, Iterable<Property>>()
						// don't be confused : method name is getEdgeNameFor, but it's usable for property name as well
						.withKey(matchingVertices)
						.withValue(updatablePath);
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		if(equalsTo.getExpected()!=null) {
			Entry<Iterable<Vertex>, Iterable<Property>> loadedEntry = load(equalsTo.getExpectedAsValue(), equalsTo.getPath());
			result.put(loadedEntry.getKey(), loadedEntry.getValue());
		}
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
