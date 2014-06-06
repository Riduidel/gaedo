package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.QueryLog;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

/**
 * Class defining how the list of vertex is to be processed to find the good ones
 * @author ndx
 *
 */
public class GraphExecutionPlan {
	protected final AbstractBluePrintsBackedFinderService<?, ?, ?> service;

	private final Iterable<Vertex> vertices;
	private final VertexTest test;
	private final SortingExpression sort;

	public GraphExecutionPlan(AbstractBluePrintsBackedFinderService<?, ?, ?> service, VertexTest test, SortingExpression sort, Iterable<Vertex> vertices) {
		super();
		this.service = service;
		this.vertices = vertices;
		this.test = test;
		this.sort = sort;
	}

	/**
	 * Get collection of vertices that correspond to test and sort expressed in this execution plan.
	 * @return
	 */
	public List<Vertex> getVertices() {
		return getVertices(vertices);
	}

	private List<Vertex> getVertices(Iterable<Vertex> examinedVertices) {
		if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
			if(examinedVertices instanceof Collection) {
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "vertex test "+test+" returned a total of "+((Collection)examinedVertices).size()+" vertices to examine");
			}
		}
		Collection<Vertex> returned = null;
		if(sort==null)
			returned = new LinkedList<Vertex>();
		else
			returned = new TreeSet<Vertex>(new SortingComparator(service, sort));
		for(Vertex v : examinedVertices) {
			if(test.matches(v)) {
				returned.add(v);
			}
			// We don't need to add specific sorting here, as it is done by the Comparator
		}
		if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
			QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "In these, "+returned.size()+" vertices matched given test "+test);
		}
		// Hopefully, CollectionUtils#asList method is smart enough to not transform a list (which is what we obtain when no sorting expression is given)
		return CollectionUtils.asList(returned);
	}
}
