package com.dooapp.gaedo.blueprints.transformers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.operations.Loader;
import com.dooapp.gaedo.blueprints.operations.Updater;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public abstract class AbstractTupleTransformer<TupleType> {

	public <DataType> Vertex getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, GraphDatabaseDriver driver, TupleType cast, CascadeType cascade,
					ObjectCache objectsBeingUpdated) {
		// First step is to build an id for given tuple by concatenating key and value id (which is hopefully done separately)
		String entryVertexId = getIdOfTuple(service.getRepository(), cast, cascade);
		// No need to memorize updated version
		String className = cast.getClass().getName();
		Vertex objectVertex = service.loadVertexFor(entryVertexId, className);
		new Updater().performUpdate(service, driver, entryVertexId, objectVertex, getContainedClass(), getContainedProperties(), cast, cascade, objectsBeingUpdated);
		if(objectVertex==null)
			objectVertex = service.loadVertexFor(entryVertexId, className);
		return objectVertex;
	}

	/**
	 * As there can be a difference between effectively contained class and claimed contained class, this method don't use the TupleType generics, but rather let subclass
	 * decide how tuples are persisted
	 * @return
	 */
	protected abstract Class<?> getContainedClass();

	protected abstract Map<Property, Collection<CascadeType>> getContainedProperties();

	/**
	 * Create a long string id by concatenating all contained properties ones
	 */
	public String getIdOfTuple(ServiceRepository repository, TupleType value, CascadeType cascade) {
		return getIdOfTuple(repository, value, getContainedProperties().keySet(), cascade);
	}

	/**
	 * Utility method allowing specific tuple transformers to provide more "optimized" versions of id building
	 * @param repository
	 * @param value
	 * @param idProperties
	 * @param cascade
	 * @return
	 */
	protected String getIdOfTuple(ServiceRepository repository, TupleType value, Iterable<Property> idProperties, CascadeType cascade) {
		StringBuilder sOut = new StringBuilder();
		Map<Property, Object> propertiesWithoutIds = new HashMap<Property, Object>();
		for(Property p : idProperties) {
			Object propertyValue = p.get(value);
			if(propertyValue!=null) {
				String id = GraphUtils.getIdOf(repository, propertyValue);
				if(id==null) {
					// we'll try to generate it : that missing id can only refer to managed object
					// (as literals and tuples can't be in that case)
					if(cascade.equals(CascadeType.PERSIST)||cascade.equals(CascadeType.MERGE)) {
						// try to create an id by directly asking the service to do so
						AbstractBluePrintsBackedFinderService service = (AbstractBluePrintsBackedFinderService) repository.get(propertyValue);
						id = service.getIdVertexId(propertyValue, true);
					}
					if(id==null)
						propertiesWithoutIds.put(p, propertyValue);
				}
				sOut.append(p.getName()).append(":").append(id).append("-");
			}
		}
		if(propertiesWithoutIds.size()>0) {
			throw TupleCantBeIdentifiedException.dueTo(value, propertiesWithoutIds);
		}
		return sOut.toString();
	}

	public Object loadObject(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, String effectiveType, Vertex key, ServiceRepository repository, ObjectCache objectsBeingAccessed) {
		TupleType tuple = instanciateTupleFor(classLoader, key);
		new Loader().loadObjectProperties(driver, strategy, classLoader, repository, key, tuple, getContainedProperties(), objectsBeingAccessed);
		return tuple;
	}

	public TupleType loadObject(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, Class effectiveClass, Vertex key, ServiceRepository repository, ObjectCache objectsBeingAccessed) {
		TupleType tuple = instanciateTupleFor(classLoader, key);
		new Loader().loadObjectProperties(driver, strategy, classLoader, repository, key, tuple, getContainedProperties(), objectsBeingAccessed);
		return tuple;
	}

	protected abstract TupleType instanciateTupleFor(ClassLoader classLoader, Vertex key);

	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+Transformer.class.getName()+"#canHandle has not yet been implemented AT ALL");
	}

	public Kind getKind() {
		return Kind.bnode;
	}
}
