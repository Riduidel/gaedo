package com.dooapp.gaedo.blueprints.indexable;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

public class IndexableGraphEnvironment extends AbstractGraphEnvironment<IndexableGraph>{

	public IndexableGraphEnvironment(GraphProvider graph) {
		super(graph);
	}

	@Override
	protected IndexableGraph createGraph(GraphProvider graphProvider) {
		return graphProvider.get(TestUtils.indexable(GraphProvider.GRAPH_DIR));
	}

	@Override
	protected <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> createServiceFor(Class<Type> beanClass, Class<InformerType> informerClass)  {
		return new IndexableGraphBackedFinderService(beanClass, informerClass, proxyInformerFactory, repository, provider, graph);
	}

}
