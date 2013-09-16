package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.projection.ProjectionBuilder;
import com.dooapp.gaedo.finders.projection.ValueFetcher;
import com.google.appengine.api.datastore.Entity;

public class ValueLoader<ValueType, DataType, InformerType extends Informer<DataType>> implements ValueFetcher {

	/**
	 * Used service
	 */
	private final DatastoreFinderService<DataType, ?> service;
	/**
	 * Used projector
	 */
	private ProjectionBuilder<ValueType, DataType, InformerType> projector;
	private InformerType informer;


	public ValueLoader(DatastoreFinderService<DataType, InformerType> service,
					ProjectionBuilder<ValueType, DataType, InformerType> projector) {
		this.service = service;
		this.informer = service.getInformer();
		this.projector = projector;
	}



	public ValueType load(Entity entity) {
		return projector.project(informer, this);
	}



	@Override
	public <Type> Type getValue(FieldInformer<Type> propertyDescriptor) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+ValueFetcher.class.getName()+"#getValue has not yet been implemented AT ALL");
	}

}
