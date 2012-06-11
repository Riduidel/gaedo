package com.dooapp.gaedo.blueprints.queries;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Basic graph query execution
 * @author ndx
 *
 */
public class BasicGraphExecutableQuery implements GraphExecutableQuery {
	private static final Logger logger = Logger.getLogger(BasicGraphExecutableQuery.class.getName());

	private VertexTest test;
	private IndexableGraph graph;
	private SortingExpression sort;
	private Class searchedClass;

	public BasicGraphExecutableQuery(IndexableGraph database, CompoundVertexTest peek, SortingExpression sortingExpression, Class searchedClass) {
		this.graph = database;
		this.test = peek;
		this.sort = sortingExpression;
		this.searchedClass = searchedClass;
	}

	/**
	 * Get a collection of vertices matching the test criteria
	 * @return
	 */
	private List<Vertex> getVertices() {
		List<Vertex> returned = new LinkedList<Vertex>();
		// First step is to locate the class node
		LiteralTransformer<Class> transformer = Literals.classes.getTransformer();
		ClassCollectionProperty classes = new ClassCollectionProperty(searchedClass);
		Vertex classVertex = GraphUtils.locateVertex(graph, Properties.vertexId, transformer.getVertexId(graph, searchedClass));
		if(classVertex==null) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "graph doesn't seem to know class vertex "+transformer.getVertexId(graph, searchedClass)+" for source class "+searchedClass.getCanonicalName());
			}
			return returned;
		}
		// Now iterate on all instances and perform test on each one
		Iterable<Edge> objectsClassEdges = classVertex.getInEdges(GraphUtils.getEdgeNameFor(classes));
		for(Edge e : objectsClassEdges) {
			Vertex examined = e.getOutVertex();
			if(test.matches(examined)) {
				returned.add(examined);
			}
		}
		return returned;
	}


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
		if(getVertices().size()>0)
			return getVertices().get(0);
		return null;
	}

}
