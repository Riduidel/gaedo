package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Optimized executable query.
 * How is it optimized ? Instead of simply searching through all vertices linked to class vertex (which always exist) through
 * Object.classes edge, this one tries to find the vertex linked to the smallest solution space. How ?
 * As an example, suppose one want to find, using our test beans, elements matching that query :
 * <pre> 
 *  AND
 *	  Posts.note ==? 4.0
 *	  Posts.author.login ==? "user login"
 *	  Object.classes contains Post.class
 * </pre>
 * This class will start by extracting query roots (vertices to which solutions of this query MUST be linked) by calling {@link #getPossibleRootsOf(VertexTest)}
 * In this example, this method call will return
 * <pre> 
 * 4.0:long => { Property[note] }
 * "user login":string => { Property[author], Property[login] }
 * Post:class => { Property[class] }
 * </pre>
 * One question remain : from these three vertices, which one will be linked (through the given property path) to the smallest number of vertices ?
 * To answer this question, the {@link #findBestRootIn(Map)} method creates a {@link VertexValueRange} object which can back-navigate the edges representing these properties
 * and directly give us the best query root.
 * 
 * @author ndx
 * 
 */
public class OptimizedGraphExecutableQuery extends AbstractGraphExecutableQuery implements GraphExecutableQuery {
	/**
	 * Construct search query, and make sure vertex tests include a test on
	 * object classes, by calling
	 * {@link #addClassSearchTo(CompoundVertexTest, Class)}
	 * 
	 * @param database
	 *            queried DB (we will get indexes on that)
	 * @param repository TODO
	 * @param vertexTest
	 *            tree of vertex tests to be executed
	 * @param sortingExpression
	 *            sorting expression used to ... well ... sort results
	 * @param searchedClass
	 *            searched value class
	 */
	public OptimizedGraphExecutableQuery(AbstractBluePrintsBackedFinderService<?, ?, ?> service, CompoundVertexTest vertexTest, SortingExpression sortingExpression) {
		super(service, addClassSearchTo(vertexTest, service.getContainedClass()), sortingExpression);
	}

	/**
	 * Make sure vertex test contains a class search test, looking for the
	 * searchedClass. TODO only add test when it's missing, and not as a
	 * mandatory step
	 * 
	 * @param vertexTest
	 * @param searchedClass
	 * @return
	 */
	private static CompoundVertexTest addClassSearchTo(CompoundVertexTest vertexTest, Class searchedClass) {
		if (vertexTest instanceof AndVertexTest) {
			CollectionContains objectClassContains = new CollectionContains(vertexTest.getDriver(),
							Arrays.asList(new Property[] { new ClassCollectionProperty(searchedClass) }), searchedClass);
			vertexTest.add(objectClassContains);
			return vertexTest;
		} else {
			// Create a new Andtest, add to it current vertex test and test on
			// class
			AndVertexTest used = new AndVertexTest(vertexTest.getDriver(), vertexTest.getPath());
			used.add(vertexTest);
			return addClassSearchTo(used, searchedClass);
		}
	}

	/**
	 * Get a quite reduced set of vertices to examine. This method return an iterable of vertices containing the ones corresponding to query solutions.
	 * This iterable is not optimal (there are elements in which than may not be query solutions). But it should be, in most cases
	 * a better solution space than the one given by {@link BasicGraphExecutableQuery} (which, as a reminder, always return all vertices linked 
	 * to the queried class through an object.classes edge).
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.executable.AbstractGraphExecutableQuery#getVerticesToExamine()
	 */
	@Override
	protected Iterable<Vertex> getVerticesToExamine() {
		// First step is to get all possible query root vertices
		Map<Vertex, Iterable<Property>> possibleRoots = getPossibleRootsOf(test);
		VertexValueRange bestMatch = findBestRootIn(possibleRoots);
		return bestMatch.getValues();
	}

	/**
 	 * Find, from all roots returned by {@link #getPossibleRootsOf(VertexTest)}, the one linked to the smallest iterable of vertices for the given
 	 * property path.
	 * @param possibleRoots a map linking vertices to the property path used to navigate from searched vertices to these vertices.
	 * @return an object containing informations about the best matching vertex (like a cache of vertices in solution space).
	 */
	private VertexValueRange findBestRootIn(Map<Vertex, Iterable<Property>> possibleRoots) {
		VertexValueRange initial = new VertexValueRange();
		Vertex root;
		for(Entry<Vertex, Iterable<Property>> entry : possibleRoots.entrySet()) {
			initial = initial.findBestMatch(entry); 
		}
		return initial;
	}

	/**
	 * Create out of given test a map linking possible query roots to the
	 * property path used to go from an object vertex to the vertex representing
	 * that root (hopefully this one is simply a copy of vertex test path).
	 * 
	 * @param test test that will be 
	 * @return
	 */
	private Map<Vertex, Iterable<Property>> getPossibleRootsOf(VertexTest test) {
		VertexRootsCollector collector = new VertexRootsCollector(service);
		test.accept(collector);
		return collector.getResult();
	}

}
