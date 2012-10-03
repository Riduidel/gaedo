package com.dooapp.gaedo.blueprints.transformers;

import java.util.Collection;
import java.util.Map;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.BluePrintsPersister;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

public abstract class AbstractTupleTransformer<TupleType> {

	protected final BluePrintsPersister persister = new BluePrintsPersister(Kind.uri);

	public <DataType> Vertex getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, TupleType cast, Map<String, Object> objectsBeingUpdated) {
		// First step is to build an id for given tuple by concatenating key and value id (which is hopefully done separately)
		String entryVertexId = getIdOfTuple(service.getRepository(), cast);
		// No need to memorize updated version
		Vertex objectVertex = service.loadVertexFor(entryVertexId);
		persister.performUpdate(service, entryVertexId, objectVertex, getContainedClass(), getContainedProperties(), cast, CascadeType.PERSIST, objectsBeingUpdated);
		if(objectVertex==null)
			objectVertex = service.loadVertexFor(entryVertexId);
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
	public String getIdOfTuple(ServiceRepository repository, TupleType value) {
		StringBuilder sOut = new StringBuilder();
		for(Property p : getContainedProperties().keySet()) {
			Object propertyValue = p.get(value);
			if(propertyValue!=null) {
				String id = GraphUtils.getIdOf(repository, propertyValue);
				sOut.append(p.getName()).append(":").append(id).append("-");
			}
		}
		return sOut.toString();
	}

	public Object loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, String effectiveType, Vertex key, ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		TupleType tuple = instanciateTupleFor(classLoader, key);
		persister.loadObjectProperties(driver, classLoader, repository, key, tuple, getContainedProperties(), objectsBeingAccessed);
		return tuple;
	}

	public TupleType loadObject(GraphDatabaseDriver driver, ClassLoader classLoader, Class effectiveClass, Vertex key, ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		TupleType tuple = instanciateTupleFor(classLoader, key);
		persister.loadObjectProperties(driver, classLoader, repository, key, tuple, getContainedProperties(), objectsBeingAccessed);
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
