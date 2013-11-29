package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Basic graph query execution
 * @author ndx
 *
 */
public class BasicGraphExecutableQuery extends AbstractGraphExecutableQuery implements GraphExecutableQuery {
	private static final Logger logger = Logger.getLogger(BasicGraphExecutableQuery.class.getName());
	
	public BasicGraphExecutableQuery(AbstractBluePrintsBackedFinderService<IndexableGraph, ?, ?> service, CompoundVertexTest peek, SortingExpression sortingExpression) {
		super(service, peek, sortingExpression);
	}

	/**
	 * Get the list of vertices to examine
	 * @return
	 */
	protected Iterable<Vertex> getVerticesToExamine() {
		List<Vertex> returned = new LinkedList<Vertex>();
		// First step is to locate the class node
		Vertex classVertex = getClassVertex();
		ClassCollectionProperty classes = new ClassCollectionProperty(getSearchedClass());
		if(classVertex==null) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "graph doesn't seem to know source class "+getSearchedClass().getCanonicalName());
			}
			return returned;
		}
		// Now iterate on all instances and perform test on each one
		Iterable<Edge> objectsClassEdges = classVertex.getEdges(Direction.IN, GraphUtils.getEdgeNameFor(classes));
		for(Edge e : objectsClassEdges) {
			returned.add(e.getVertex(Direction.OUT));
		}
		return returned;
	}

	protected Vertex getClassVertex() {
		LiteralTransformer<Class> transformer = Literals.classes.getTransformer();
		return service.loadVertexFor(transformer.getVertexId(getSearchedClass()), Class.class.getName());
	}
}
