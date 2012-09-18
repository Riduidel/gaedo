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
	public Vertex loadVertexFor(String objectVertexId) {
		return database.getVertex(objectVertexId);
	}

	@Override
	protected QueryStatement<DataType, Informer<DataType>> createQueryStatement(QueryBuilder<Informer<DataType>> query) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractFinderService.class.getName()+"#createQueryStatement has not yet been implemented AT ALL");
	}

	@Override
	public String getIdOfVertex(Vertex objectVertex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractBluePrintsBackedFinderService.class.getName()+"#getIdOfVertex has not yet been implemented AT ALL");
	}

	@Override
	protected Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractBluePrintsBackedFinderService.class.getName()+"#createEmptyVertex has not yet been implemented AT ALL");
	}

	@Override
	protected String getEffectiveType(Vertex vertex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractBluePrintsBackedFinderService.class.getName()+"#getEffectiveType has not yet been implemented AT ALL");
	}

	@Override
	protected void setValue(Vertex vertex, Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractBluePrintsBackedFinderService.class.getName()+"#setValue has not yet been implemented AT ALL");
	}

	@Override
	protected Object getValue(Vertex vertex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+AbstractBluePrintsBackedFinderService.class.getName()+"#getValue has not yet been implemented AT ALL");
	}


}
