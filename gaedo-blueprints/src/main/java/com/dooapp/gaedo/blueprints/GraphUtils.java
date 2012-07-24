package com.dooapp.gaedo.blueprints;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TupleTransformer;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

public class GraphUtils {

	/**
	 * Helper function locating first matching vertex with the given property value
	 * @param graph
	 * @param property
	 * @param object
	 * @return
	 */
	public static Vertex locateVertex(IndexableGraph graph, String property, Object value) {
		CloseableSequence<Vertex> matching = graph.getIndex(Index.VERTICES, Vertex.class).get(property, value);
		if(matching.hasNext()) {
			return matching.next();
		} else {
			return null;
		}
	}

	/**
	 * Get vertex for which the given declared property has the given declared value
	 * @param graph source graph
	 * @param p property to get associated vertex for
	 * @param value property value
	 * @return vertex corresponding to property, or null if none found
	 */
	public static Vertex locateVertex(IndexableGraph graph, Properties p, Object value) {
		return locateVertex(graph, p.name(), value);
	}

	/**
	 * Generate edge name from property infos
	 * @param p source property
	 * @return an edge name (by default property container class name + "." + property name
	 */
	public static String getEdgeNameFor(Property p) {
		return p.getDeclaringClass().getSimpleName()+"."+p.getName();
	}

	/**
	 * Create a vertex out of a basic object. if object is of a simple type, we'll use value as id. Elsewhere, we will generate an id for object
	 * @param database database in which vertex will be stored
	 * @param value
	 * @return
	 */
	public static Vertex getVertexForLiteral(IndexableGraph database, Object value) {
		Vertex returned = null;
		// Now distinct behaviour between known objects and unknown ones
		Class<? extends Object> valueClass = value.getClass();
		if(Literals.containsKey(valueClass)) {
			LiteralTransformer transformer = Literals.get(valueClass);
			returned = transformer.getVertexFor(database, valueClass.cast(value));
		} else {
			throw new ObjectIsNotARealLiteralException(value, valueClass);
			// TODO do not forget to set id property
		}
		return returned;
	}

	/**
	 * Create an object instance from a literal vertex compatible with this service contained class
	 * @param classLoader class loader used to find class
	 * @param key vertex containing object id
	 * @param repository service repository, used to disambiguate subclass of literal and managed class
	 * @param objectsBeingAccessed 
	 * @return a fresh instance, with only id set
	 */
	public static Object createInstance(ClassLoader classLoader, Vertex key, ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		String effectiveType = key.getProperty(Properties.type.name()).toString();
		if(classLoader==null) {
			throw new UnspecifiedClassLoader();
		}
		try {
			if(Literals.containsKey(classLoader, effectiveType) && !repository.containsKey(effectiveType)) {
				LiteralTransformer transformer = Literals.get(classLoader, effectiveType);
				return transformer.loadObject(classLoader, effectiveType, key);
			} else {
				Class<?> type = classLoader.loadClass(effectiveType);
				if(Tuples.containsKey(type) && !repository.containsKey(type)) {
					// Tuples are handled the object way (easier, but more dangerous
					TupleTransformer transformer = Tuples.get(type);
					return transformer.loadObject(classLoader, type, key, repository, objectsBeingAccessed);
				} else {
					return  type.newInstance();
				}
			}
		} catch(Exception e) {
			throw new UnableToCreateException(effectiveType, e);
		}
	}

	/**
	 * Get an id for any object, provided one property can be used to define an id on it
	 * @param database source database
	 * @param declaredClass class which the object is a declared member of. it may not be its effective class, but its anyway the class we manage it with
	 * @param object used object
	 * @param idProperty id property
	 * @return a composite id made of id container class, and the result of {@link LiteralTransformer#getVertexId(com.tinkerpop.blueprints.pgm.Graph, Object)}
	 */
	public static String getIdVertexId(IndexableGraph database, Class<?> declaredClass, Object object, Property idProperty) {
		Object objectId = idProperty.get(object);
		return getIdOfLiteral(database, declaredClass, idProperty, objectId);
	}
	
	/**
	 * get an id value for the given object whatever the object is
	 * @param repository
	 * @param value
	 * @return
	 */
	public static <DataType> String getIdOf(IndexableGraph graph, ServiceRepository repository, DataType value) {
		Class<? extends Object> valueClass = value.getClass();
		if(repository.containsKey(valueClass)) {
			BluePrintsBackedFinderService<DataType, ?> service = (BluePrintsBackedFinderService<DataType, ?>) repository.get(valueClass);
			// All ids are string, don't worry about it
			return service.getIdOf(value).toString();
		} else if(Literals.containsKey(valueClass)) {
			return getIdOfLiteral(graph, valueClass, null, value);
		} else if(Tuples.containsKey(valueClass)) {
			return getIdOfTuple(graph, repository, valueClass, value);
		} else {
			throw new ImpossibleToGetIdOfUnknownType(valueClass);
		}
	}

	/**
	 * Get the value of the vertex id for the given literal
	 * @param database used graph
	 * @param declaredClass declared object class
	 * @param idProperty gives the declared type of id (which may differ from primitive types, where user may give an integer instead of a long, as an example). Notice that, 
	 * contrary to most of gaedo code, this field can be null 
	 * @param objectId object id value
	 * @return the value used by {@link Properties#vertexId} to identify the vertex associated to that object
	 */
	public static String getIdOfLiteral(IndexableGraph database, Class<?> declaredClass, Property idProperty, Object objectId) {
		StringBuilder sOut = new StringBuilder();
		sOut.append(declaredClass.getCanonicalName()).append(":");
		if(idProperty==null) {
			sOut.append(Literals.get(declaredClass).getVertexId(database, objectId));
		} else {
			sOut.append(Literals.get(idProperty.getType()).getVertexId(database, objectId));
		}
		return sOut.toString();
	}

	/**
	 * Get the value of the vertex id for the given object
	 * @param database used graph
	 * @param declaredClass declared object class
	 * @param idProperty gives the declared type of id (which may differ from primitive types, where user may give an integer instead of a long, as an example). Notice that, 
	 * contrary to most of gaedo code, this field can be null 
	 * @param value object id value
	 * @return the value used by {@link Properties#vertexId} to identify the vertex associated to that object
	 */
	public static String getIdOfTuple(IndexableGraph database, ServiceRepository repository, Class<?> declaredClass, Object value) {
		return Tuples.get(declaredClass).getIdOfTuple(database, repository, value);
	}

	/**
	 * Generates a vertex for the given tuple
	 * @param bluePrintsBackedFinderService source service, some informations may be extracted from it
	 * @param repository service repository for non literal values
	 * @param value tuple to persist
	 * @param objectsBeingUpdated map of objects already being accessed. Links object id to object
	 * @return the
	 */
	public static Vertex getVertexForTuple(BluePrintsBackedFinderService<?, ?> service, ServiceRepository repository, Object value, Map<String, Object> objectsBeingUpdated) {
		Vertex returned = null;
		// Now distinct behaviour between known objects and unknown ones
		Class<? extends Object> valueClass = value.getClass();
		if(Tuples.containsKey(valueClass)) {
			TupleTransformer transformer = Tuples.get(valueClass);
			returned = transformer.getVertexFor(service, valueClass.cast(value), objectsBeingUpdated);
		} else {
			throw new ObjectIsNotARealTupleException(value, valueClass);
			// TODO do not forget to set id property
		}
		return returned;
	}

	public static Collection<CascadeType> extractCascadeOf(CascadeType[] cascade) {
		Set<CascadeType> returned = new HashSet<CascadeType>();
		returned.addAll(Arrays.asList(cascade));
		if(returned.contains(CascadeType.ALL)) {
			returned.remove(CascadeType.ALL);
			returned.add(CascadeType.MERGE);
			returned.add(CascadeType.PERSIST);
			returned.add(CascadeType.REFRESH);
			returned.add(CascadeType.REMOVE);
		}
		return returned;
	}

	/**
	 * Create a vertex in graph with the given properties. Notice {@link Properties#value} won't be set here.
	 * @param database database in which vertex is created
	 * @param vertexId vertex id (associated to {@link Properties#vertexId})
	 * @param kind vertex kind (associated to {@link Properties#kind})
	 * @param type (associated to {@link Properties#type})
	 * @return the newly created vertex (which DOESN'T contain the {@link Properties#value} property)
	 */
	public static Vertex createVertexWithoutValue(IndexableGraph database, Object vertexId, Kind kind, Class<? extends Object> type) {
		Vertex returned = database.addVertex(vertexId);
		saveVertexValues(returned, vertexId, kind, type);
		return returned;
	}

	public static void saveVertexValues(Vertex returned, Object vertexId, Kind kind, Class<? extends Object> type) {
		returned.setProperty(Properties.vertexId.name(), vertexId);
		returned.setProperty(Properties.kind.name(), kind.name());
		returned.setProperty(Properties.type.name(), type.getName());
	}

}
