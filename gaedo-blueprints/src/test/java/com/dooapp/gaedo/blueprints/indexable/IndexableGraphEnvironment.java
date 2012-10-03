package com.dooapp.gaedo.blueprints.indexable;

import org.openrdf.repository.sail.SailRepository;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class IndexableGraphEnvironment extends AbstractGraphEnvironment<IndexableGraph>{

	public IndexableGraphEnvironment(GraphProvider graph) {
		super(graph);
	}

	@Override
	protected IndexableGraph createGraph(GraphProvider graphProvider) {
		return graphProvider.get(usablePath());
	}

	@Override
	protected <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> createServiceFor(Class<Type> beanClass, Class<InformerType> informerClass)  {
		return new IndexableGraphBackedFinderService(beanClass, informerClass, proxyInformerFactory, repository, provider, graph);
	}

	@Override
	public SailRepository getRepository() {
		return new SailRepository(new GraphSail(graph));
	}

	@Override
	public String usablePath() {
		return TestUtils.indexable(GraphProvider.GRAPH_DIR);
	}

}
