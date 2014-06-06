package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.QueryLog;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.SortingExpression;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

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
 * To answer this question, the {@link #findBestRootIn(Map)} method creates a {@link VertexSet} object which can back-navigate the edges representing these properties
 * and directly give us the best query root.
 *
 * @author ndx
 *
 */
public class OptimizedGraphExecutableQuery<GraphType extends IndexableGraph> extends AbstractGraphExecutableQuery<GraphType> implements GraphExecutableQuery {
	/**
	 * Construct search query, and make sure vertex tests include a test on
	 * object classes, by calling
	 * {@link #addClassSearchTo(CompoundVertexTest, Class)}
	 *
	 * @param database
	 *            queried DB (we will get indexes on that)
	 * @param vertexTest
	 *            tree of vertex tests to be executed
	 * @param sortingExpression
	 *            sorting expression used to ... well ... sort results
	 * @param searchedClass
	 *            searched value class
	 */
	public OptimizedGraphExecutableQuery(AbstractBluePrintsBackedFinderService<GraphType, ?, ?> service, CompoundVertexTest vertexTest, SortingExpression sortingExpression) {
		super(service, addDefaultSearchTo(service, vertexTest), sortingExpression);
	}

	/**
	 * Adds a default search mechanism to that query.
	 * That default search mechanism allows queries to work even when no equals query is specified.
	 * Effective used default seearch depends upon {@link GraphMappingStrategy}
	 *
	 * @param service
	 * @param vertexTest
	 * @return
	 */
	private static CompoundVertexTest addDefaultSearchTo(AbstractBluePrintsBackedFinderService<?, ?, ?> service, CompoundVertexTest vertexTest) {
		return service.getStrategy().addDefaultSearchTo(vertexTest);
	}

	/**
	 * Get a quite reduced set of vertices to examine. This method return an iterable of vertices containing the ones corresponding to query solutions.
	 * This iterable is not optimal (there are elements in which than may not be query solutions). But it should be, in most cases
	 * a better solution space than the one given by previous BasicGraphExecutableQuery (which, as a reminder, always return all vertices linked
	 * to the queried class through an object.classes edge).
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.executable.AbstractGraphExecutableQuery#getVerticesToExamine()
	 */
	@Override
	public GraphExecutionPlan getExecutionPlan() {
		Iterable<Vertex> verticesToExamine = getVerticesToExamine();
		VertexTest toUse = test;
		if(verticesToExamine==null) {
			// First step is to get all possible query root vertices
			SortedMap<VertexSet, VertexTest> possibleRoots = getPossibleRootsOf(test);
			// extract the set from the map. Too bad it can't be provided directly by Java
			SortedSet<VertexSet> rootsSet = new TreeSet<VertexSet>(possibleRoots.comparator());
			rootsSet.addAll(possibleRoots.keySet());
			VertexSet bestMatch = findBestRootIn(rootsSet);
			toUse = possibleRoots.get(bestMatch);
			LazyLoader vertices = bestMatch.getVertices();
			verticesToExamine = vertices.get();
			if(QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
				StringBuilder sOut = new StringBuilder();
				sOut.append("query roots for test ").append(test).append("\nare the ").append(vertices.size()).append(" following vertices");
				for(Vertex v : verticesToExamine) {
					sOut.append("\n").append(GraphUtils.toString(v));
				}
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, sOut.toString());
			}
		}
		return new GraphExecutionPlan(service, toUse, sort, verticesToExamine);
	}

	/**
	 * Override of the list of vertices to load. Allow bypassing of the optimized part of {@link #getExecutionPlan()}.
	 * Conclusion is obvious : unless you really know what you do, please don't override it creatively.
	 * @return
	 */
	public Iterable<Vertex> getVerticesToExamine() {
		return null;
	}

	/**
 	 * Find, from all roots returned by {@link #getPossibleRootsOf(VertexTest)}, the one linked to the smallest iterable
 	 * of vertices for the given* property path. To do so, we simply call VertexSet#canGoBack and VertexSet#goBack on
 	 * head element of possibleRoots as long as canGoBack return true. Indeed, when VertexSet#canGoBack return false, it emans that {@link VertexSet}
 	 * is totally resolved and can be used for testing vertices out.
	 * @param possibleRoots a list of vertex value range allowing lazy loading of matching vertices
	 * @return an object containing informations about the best matching vertex (like a cache of vertices in solution space).
	 */
	public VertexSet findBestRootIn(SortedSet<VertexSet> possibleRoots) {
		VertexSet tested = possibleRoots.first();
		while(tested.canGoBack()) {
			possibleRoots.remove(tested);
			tested.goBack();
			/* notice how we just remvoed/put back the vertex set in the list of possible roots ?
			 * Well, it's an optimization : possibleRoots is sorted by smaller first. So each turn we expand the smallest set.
			 * In the worst-case scenario, all will be expanded. But I hope it won't happen that often.
			 */
			possibleRoots.add(tested);
		}
		return tested;
	}

	/**
	 * Create out of given test a map linking possible query roots to the
	 * property path used to go from an object vertex to the vertex representing
	 * that root (hopefully this one is simply a copy of vertex test path).
	 *
	 * @param test test that will be
	 * @return
	 */
	public SortedMap<VertexSet, VertexTest> getPossibleRootsOf(VertexTest test) {
		return new VertexRootsCollector(service).getSetsToProcessedTests(test);
	}

}
