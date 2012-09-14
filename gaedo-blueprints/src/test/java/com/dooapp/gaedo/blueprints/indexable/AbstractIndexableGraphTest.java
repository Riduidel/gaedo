package com.dooapp.gaedo.blueprints.indexable;

import com.dooapp.gaedo.blueprints.AbstractGraphTest;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

public class AbstractIndexableGraphTest extends AbstractGraphTest<IndexableGraph> {

	public AbstractIndexableGraphTest(GraphProvider graph) {
		super(graph);
	}

	@Override
	protected IndexableGraph createGraph(GraphProvider graphProvider) {
		return graphProvider.get(TestUtils.indexable(GraphProvider.GRAPH_DIR));
	}

	@Override
	protected <Type> FinderCrudService<Type, Informer<Type>> createServiceFor(Class<Type> beanClass, Class<? extends Informer<Type>> informerClass) {
		return new IndexableGraphBackedFinderService(beanClass, informerClass, proxyInformerFactory, repository, provider, graph);
	}

}
