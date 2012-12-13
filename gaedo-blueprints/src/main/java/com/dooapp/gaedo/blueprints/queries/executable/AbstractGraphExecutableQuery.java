package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.LinkedList;
import java.util.List;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.finders.SortingExpression;
import com.tinkerpop.blueprints.pgm.Vertex;

public abstract class AbstractGraphExecutableQuery implements GraphExecutableQuery {

	protected final VertexTest test;
	protected final SortingExpression sort;
	protected final AbstractBluePrintsBackedFinderService<?, ?, ?> service;

	public AbstractGraphExecutableQuery(AbstractBluePrintsBackedFinderService<?, ?, ?> service, CompoundVertexTest vertexTest, SortingExpression sortingExpression) {
		this.service = service;
		this.test = vertexTest;
		this.sort = sortingExpression;
	}

	

	/**
	 * Get a collection of vertices matching the test criteria
	 * @return
	 */
	protected List<Vertex> getVertices() {
		List<Vertex> returned = new LinkedList<Vertex>();
		Iterable<Vertex> examinedVertices = getVerticesToExamine();
		for(Vertex v : examinedVertices) {
			if(test.matches(v)) {
				returned.add(v);
			}
			// TODO add sorting here
		}
		return returned;
	}

	/**
	 * Get the list of vertices to be examined
	 * @return
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
