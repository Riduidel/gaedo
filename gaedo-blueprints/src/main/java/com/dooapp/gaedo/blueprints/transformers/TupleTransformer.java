package com.dooapp.gaedo.blueprints.transformers;

import java.util.Map;

import com.dooapp.gaedo.blueprints.BluePrintsBackedFinderService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

public interface TupleTransformer<Type> extends Transformer {

	/**
	 * Build (or find) the vertex associated to the given tuple
	 * @param service source service
	 * @param cast casted value
	 * @param objectsBeingUpdated map of already accessed objects
	 * @return
	 */
	public <DataType> Vertex getVertexFor(BluePrintsBackedFinderService<DataType, ?> service, Type cast, Map<String, Object> objectsBeingUpdated);
	
	/**
	 * Create an identifier for tuple value, which can be done in any fashion
	 */
	public String getIdOfTuple(IndexableGraph graph, ServiceRepository repository, Type value);

	/**
	 * Load object from vertice, using all provided informations
	 * @param classLoader
	 * @param effectiveClass
	 * @param key
	 * @param repository
	 * @param objectsBeingAccessed
	 * @return
	 */
	public Object loadObject(ClassLoader classLoader, Class effectiveClass, Vertex key, ServiceRepository repository, Map<String, Object> objectsBeingAccessed);

}
