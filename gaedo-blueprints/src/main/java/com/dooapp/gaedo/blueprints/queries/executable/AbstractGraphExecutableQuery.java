package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.QueryLog;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

public abstract class AbstractGraphExecutableQuery<GraphType extends IndexableGraph> implements GraphExecutableQuery {

	protected final VertexTest test;
	protected final SortingExpression sort;
	protected final AbstractBluePrintsBackedFinderService<GraphType, ?, ?> service;

	public AbstractGraphExecutableQuery(AbstractBluePrintsBackedFinderService<GraphType, ?, ?> service, CompoundVertexTest vertexTest, SortingExpression sortingExpression) {
		this.service = service;
		this.test = vertexTest;
		this.sort = sortingExpression;
	}



	/**
	 * Get a collection of vertices matching the test criteria
	 * @return
	 */
	protected List<Vertex> getVertices() {
		Collection<Vertex> unsortedVertices = null;
		if(sort==null)
			unsortedVertices = new LinkedList<Vertex>();
		else
			unsortedVertices = new TreeSet<Vertex>(new SortingComparator(service, sort));
		Iterable<Vertex> examinedVertices = getVerticesToExamine();
		if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
			if(examinedVertices instanceof Collection) {
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "vertex test "+test+" returned a total of "+((Collection)examinedVertices).size()+" vertices to examine");
			}
		}
		for(Vertex v : examinedVertices) {
			if(test.matches(v)) {
				unsortedVertices.add(v);
			}
			// We don't need to add specific sorting here, as it is done by the Comparator
		}
		if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
			QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "In these, "+unsortedVertices.size()+" vertices matched given test "+test);
		}
		// Hopefully, CollectionUtils#asList method is smart enough to not transform a list (which is what we obtain when no sorting expression is given)
		return CollectionUtils.asList(unsortedVertices);
	}

	/**
	 * Get the list of vertices to be examined.
	 * This method is supposed to give to tests all the vertices in graph that may be valid results for tests. In other words, it must
	 * result a superset of all valid vertices. Obviously, the goal is to find fast the smaller superset.
	 * @return an unordered superset of matching vertices.
	 */
	protected abstract Iterable<Vertex> getVerticesToExamine();



	@Override
	public int count() {
		return getVertices().size();
	}


	@Override
	public Iterable<Vertex> get(int start, int end) {
		return getVertices().subList(start, end);
	}

	@Override
	public Iterable<Vertex> getAll() {
		return getVertices();
	}

	@Override
	public Vertex getVertex() {
		List<Vertex> vertices = getVertices();
		if(vertices.size()>0)
			return vertices.get(0);
		return null;
	}

	protected Class getSearchedClass() {
		return service.getContainedClass();
	}



	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName()).append(" [");
		if (test != null) {
			builder.append("test=");
			builder.append(test);
			builder.append(", ");
		}
		if (sort != null) {
			builder.append("sort=");
			builder.append(sort);
		}
		builder.append("]");
		return builder.toString();
	}


}
