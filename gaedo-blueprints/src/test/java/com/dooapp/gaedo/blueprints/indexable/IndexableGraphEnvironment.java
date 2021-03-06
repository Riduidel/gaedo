package com.dooapp.gaedo.blueprints.indexable;

import java.util.SortedSet;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.IndexableGraph;

public class IndexableGraphEnvironment extends AbstractGraphEnvironment<IndexableGraph> {

	public IndexableGraphEnvironment(GraphProvider graph) {
		super(graph);
	}

	@Override
	protected IndexableGraph createGraph(GraphProvider graphProvider) {
		return graphProvider.get(usablePath());
	}

	@Override
	public <Type, InformerType extends Informer<Type>> InViewService<Type, InformerType, SortedSet<String>> doCreateServiceFor(
					Class<Type> beanClass,
					Class<InformerType> informerClass,
					StrategyType strategy)  {
		return new IndexableGraphBackedFinderService<Type, InformerType>(
						getGraph(),
						beanClass,
						informerClass,
						getInformerFactory(),
						getServiceRrepository(),
						getProvider(),
						strategy);
	}

	@Override
	public String usablePath() {
		return TestUtils.indexable(GraphProvider.GRAPH_DIR);
	}

}
