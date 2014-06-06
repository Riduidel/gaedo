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
		GraphExecutionPlan examinedVertices = getExecutionPlan();
		return examinedVertices.getVertices();
	}

	/**
	 * Get the list of vertices to be examined.
	 * This method is supposed to give to tests all the vertices in graph that may be valid results for tests. In other words, it must
	 * result a superset of all valid vertices. Obviously, the goal is to find fast the smaller superset.
	 * @return an unordered superset of matching vertices.
	 */
	public abstract GraphExecutionPlan getExecutionPlan();

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



	/**
	 * @return the test
	 * @category getter
	 * @category test
	 */
	public VertexTest getTest() {
		return test;
	}


}
