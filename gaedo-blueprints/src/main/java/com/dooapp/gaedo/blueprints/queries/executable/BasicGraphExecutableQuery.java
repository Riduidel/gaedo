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
	public Iterable<Vertex> getVerticesToExamine() {
		throw new UnsupportedOperationException("not yet rewritten, as I don't yet know how to serialize literal collections ... and classes ARE literals");
	}
}
