package com.dooapp.gaedo.blueprints.sail;

import com.dooapp.gaedo.blueprints.AbstractGraphTest;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.SailGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public abstract class AbstractSailGraphTest extends AbstractGraphTest<SailGraph> {

	public AbstractSailGraphTest(GraphProvider graph) {
		super(graph);
	}


	protected <Type> FinderCrudService<Type, Informer<Type>> createServiceFor(Class<Type> beanClass, Class<? extends Informer<Type>> informerClass) {
		return new SailGraphBackedFinderService(beanClass, informerClass, proxyInformerFactory, repository, provider, graph);
	}


	@Override
	protected SailGraph createGraph(GraphProvider graphProvider) {
		return new SailGraph(new GraphSail(graphProvider.get(TestUtils.sail(GraphProvider.GRAPH_DIR))));
	}
}
