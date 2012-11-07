package com.dooapp.gaedo.blueprints.sail;

import java.util.SortedSet;

import org.openrdf.repository.sail.SailRepository;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class SailGraphEnvironment extends AbstractGraphEnvironment<SailGraph> {

	public SailGraphEnvironment(GraphProvider graph) {
		super(graph);
	}


	public <Type, InformerType extends Informer<Type>> InViewService<Type, InformerType, SortedSet<String>> doCreateServiceFor(Class<Type> beanClass, Class<InformerType> informerClass, StrategyType strategy) {
		return new SailGraphBackedFinderService(graph,
						beanClass,
						informerClass,
						getInformerFactory(),
						getServiceRrepository(),
						getProvider(),
						strategy);
	}


	@Override
	protected SailGraph createGraph(GraphProvider graphProvider) {
		return new SailGraph(new GraphSail(graphProvider.get(usablePath())));
	}


	public String usablePath() {
		return TestUtils.sail(GraphProvider.GRAPH_DIR);
	}


	@Override
	public SailRepository getSailRepository() {
		SailRepository sailRepository = new SailRepository(graph.getRawGraph());
		return sailRepository;
	}

}
