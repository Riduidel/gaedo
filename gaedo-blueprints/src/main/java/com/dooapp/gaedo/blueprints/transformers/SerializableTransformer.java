package com.dooapp.gaedo.blueprints.transformers;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Serializable handling is quite particular. Instead of saving a simple node type, this transformer checks the contained value and try to save it according to its inner type 
 * (using {@link LiteralTransformer}, others {@link TupleTransformer} and services). This provide compact DB with good performances, at the cost of a complex class ... this one.
 * @author ndx
 *
 */
public class SerializableTransformer implements TupleTransformer<Serializable> {
	private Serializable readSerializable(String valueString) {
		ByteArrayInputStream stream = new ByteArrayInputStream(valueString.getBytes());
		XMLDecoder decoder = new XMLDecoder(stream);
		return (Serializable) decoder.readObject();
	}

	private String writeSerializable(Serializable value) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(stream);
		encoder.writeObject(value);
		encoder.close();
		try {
			stream.close();
			return new String(stream.toByteArray());
		} catch (IOException e) {
			throw new UnableToStoreSerializableException("impossible to store serializable value "+value, e);
		}
	}

	/**
	 * Get vertex for the value. To get that vertex, we first check the effective value type. If a literal or a tuple (other than serializable), we use that vertex value.
	 * If a managed object, we use the service associated to that object. Finally, if an unknown serializable ... well, we serialize it
	 * @param service
	 * @param cast
	 * @param objectsBeingUpdated
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.TupleTransformer#getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?>, java.lang.Object, java.util.Map)
	 */
	@Override
	public <DataType> Vertex getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Serializable cast, CascadeType cascade,
					Map<String, Object> objectsBeingUpdated) {
		ServiceRepository repository = service.getRepository();
		// some first-level check to see if someone else than this transformer has any knowledge of value (because, well, this id will be longer than hell)
		Class<? extends Serializable> valueClass = cast.getClass();
		if(Tuples.containsKey(valueClass)) {
			if(Tuples.get(valueClass).equals(this)) {
				return getVertextForUnknownSerializable(service.getDriver(), repository, cast);
			}
		}
		// Gently ask service for effective access to value
		return service.getVertexFor(cast, null, objectsBeingUpdated);
	}

	
	private Vertex getVertextForUnknownSerializable(GraphDatabaseDriver database, ServiceRepository repository, Serializable value) {
		String serialized = writeSerializable(value);
		// Then indexed vertex id (for neo4j, typically)
		Vertex returned = database.loadVertexFor(serialized, value.getClass().getName());
		// Finally create vertex
		if(returned==null) {
			returned = database.createEmptyVertex(Serializable.class, serialized, value);
			database.setValue(returned, serialized);
		}
		return returned;
	}

	@Override
	public String getIdOfTuple(ServiceRepository repository, Serializable value) {
		// some first-level check to see if someone else than this transformer has any knowledge of value (because, well, this id will be longer than hell)
		Class<? extends Serializable> valueClass = value.getClass();
		if(Tuples.containsKey(valueClass)) {
			if(Tuples.get(valueClass).equals(this)) {
				return writeSerializable(value);
			}
		}
		// Delegate to the rest of the world
		return GraphUtils.getIdOf(repository, value);
	}

	/**
	 * For loading object, reverse job iof persisting is done, but way simpler, as using other persistences mechanisms allows us to load known serializable
	 * using their assocaited literal transformers/services, which is WAAAAYYYYYYY cooler.
	 * So, this method simple job is just to read value and deserialize it. Nice, no ?
	 * @param classLoader
	 * @param effectiveClass
	 * @param key
	 * @param repository
	 * @param objectsBeingAccessed
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.TupleTransformer#loadObject(java.lang.ClassLoader, java.lang.Class, com.tinkerpop.blueprints.pgm.Vertex, com.dooapp.gaedo.finders.repository.ServiceRepository, java.util.Map)
	 */
	@Override
	public Object loadObject(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, Class effectiveClass, Vertex key, ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		return readSerializable(driver.getValue(key).toString());
	}

	@Override
	public Object loadObject(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, String effectiveType, Vertex key, ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		return readSerializable(driver.getValue(key).toString());
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+Transformer.class.getName()+"#canHandle has not yet been implemented AT ALL");
	}

	@Override
	public Kind getKind() {
		return Kind.bnode;
	}
}
