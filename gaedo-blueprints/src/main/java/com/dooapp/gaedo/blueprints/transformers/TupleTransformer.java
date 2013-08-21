package com.dooapp.gaedo.blueprints.transformers;

import java.util.Map;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public interface TupleTransformer<Type> extends Transformer {

	/**
	 * Build (or find) the vertex associated to the given tuple
	 * @param service source service
	 * @param cast casted value
	 * @param cascade cascade to be used for that operation
	 * @param objectsBeingUpdated map of already accessed objects
	 * @return
	 */
	public <DataType> com.tinkerpop.blueprints.Vertex getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Type cast, CascadeType cascade, ObjectCache objectsBeingUpdated);

	/**
	 * Create an identifier for tuple value, which can be done in any fashion
	 */
	public String getIdOfTuple(ServiceRepository repository, Type value);

	/**
	 * Load object from vertice, using all provided informations
	 * @param strategy TODO
	 * @param classLoader
	 * @param effectiveClass
	 * @param key
	 * @param repository
	 * @param objectsBeingAccessed
	 * @return
	 */
	public Object loadObject(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, Class effectiveClass, Vertex key, ServiceRepository repository, ObjectCache objectsBeingAccessed);


	/**
	 * Load object from vertice, using all provided informations
	 * @param strategy TODO
	 * @param classLoader
	 * @param key
	 * @param repository
	 * @param objectsBeingAccessed
	 * @param effectiveClass
	 * @return
	 */
	public Object loadObject(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, String effectiveType, Vertex key, ServiceRepository repository, ObjectCache objectsBeingAccessed);

	public Kind getKind();

}
