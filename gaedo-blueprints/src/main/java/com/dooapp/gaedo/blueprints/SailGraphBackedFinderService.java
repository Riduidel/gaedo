package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.AbstractFinderService;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

public class SailGraphBackedFinderService<DataType, InformerType extends Informer<DataType>> extends AbstractBluePrintsBackedFinderService<SailGraph, DataType, Informer<DataType>>{

	public SailGraphBackedFinderService(Class<DataType> containedClass, Class<Informer<DataType>> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, SailGraph graph) {
		super(graph,containedClass, informerClass, factory, repository, provider);
	}

	@Override
	protected Vertex loadVertexFor(String objectVertexId) {
		return database.getVertex(objectVertexId);
	}

	@Override
	protected QueryStatement<DataType, Informer<DataType>> createQueryStatement(QueryBuilder<Informer<DataType>> query) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractFinderService.class.getName()+"#createQueryStatement has not yet been implemented AT ALL");
	}


}
