package com.dooapp.gaedo.blueprints.sail;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.SailGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class SailGraphEnvironment extends AbstractGraphEnvironment<SailGraph> {

	public SailGraphEnvironment(GraphProvider graph) {
		super(graph);
	}


	protected <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> createServiceFor(Class<Type> beanClass, Class<InformerType> informerClass) {
		return new SailGraphBackedFinderService(beanClass, informerClass, proxyInformerFactory, repository, provider, graph);
	}


	@Override
	protected SailGraph createGraph(GraphProvider graphProvider) {
		return new SailGraph(new GraphSail(graphProvider.get(TestUtils.sail(GraphProvider.GRAPH_DIR))));
	}
}
