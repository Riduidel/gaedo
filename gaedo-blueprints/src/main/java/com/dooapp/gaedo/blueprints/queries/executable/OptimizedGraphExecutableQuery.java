package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
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
 * Optimized executable query. This one defers from the basic one by trying to
 * optimized the way the graph is searched. Let me make it clearer : Instead of
 * using the Class vertex as first vertex, this one looks in the various vertex
 * roots the one with the less number of connections. Obviously, this requires
 * us to count the number of edges linking those roots to objects, and try to
 * evaluate the number of objects of this class that will match. However, we do
 * consider here this to be optimal when the number of vertices to search is
 * great.
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
	public OptimizedGraphExecutableQuery(IndexableGraphBackedFinderService<?, ?> service, CompoundVertexTest vertexTest, SortingExpression sortingExpression) {
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
			CollectionContains objectClassContains = new CollectionContains(vertexTest.getRepository(),
							Arrays.asList(new Property[] { new ClassCollectionProperty(searchedClass) }), searchedClass);
			vertexTest.add(objectClassContains);
			return vertexTest;
		} else {
			// Create a new Andtest, add to it current vertex test and test on
			// class
			AndVertexTest used = new AndVertexTest(vertexTest.getRepository(), vertexTest.getPath());
			used.add(vertexTest);
			return addClassSearchTo(used, searchedClass);
		}
	}

	@Override
	protected Iterable<Vertex> getVerticesToExamine() {
		// First step is to get all possible query root vertices
		Map<Vertex, Iterable<Property>> possibleRoots = getPossibleRootsOf(test);
		VertexValueRange bestMatch = findBestRootIn(possibleRoots);
		return bestMatch.getValues();
	}

	/**
	 * Get best vertex for performing query. The best vertex is usually the one with the lower number of of edges associated to property path last fragment 
	 * @param possibleRoots
	 * @return
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
