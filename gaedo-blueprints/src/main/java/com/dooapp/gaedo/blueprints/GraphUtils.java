package com.dooapp.gaedo.blueprints;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;

import com.dooapp.gaedo.blueprints.annotations.GraphProperty;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TupleTransformer;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class GraphUtils {
	private static final String GAEDO_PREFIX = "https://github.com/Riduidel/gaedo/";

	/**
	 * Ontologic context used by all gaedo graph elements.
	 */
	public static final String GAEDO_CONTEXT = GAEDO_PREFIX+"visible";
	
	/**
	 * Ontologic context used by gaedo graph elements that we want to keep hidden. Those elements should never be exported.
	 * To make sure this works well, this context is set to null. Crazy no ?
	 */
	public static final String GAEDO_HIDDEN_CONTEXT = GAEDO_PREFIX+"hidden";

	private static final Logger logger = Logger.getLogger(GraphUtils.class.getName());
	
	public static Object asSailProperty(String context) {
		return GraphSail.URI_PREFIX+" "+context;
	}

	/**
	 * Generate edge name from property infos. Notice generated edge name will first be searched in property annotations, and only 
	 * if none compatile found by generating a basic property name
	 * @param p source property
	 * @return an edge name (by default property container class name + "." + property name
	 */
	public static String getEdgeNameFor(Property p) {
		if(p.getAnnotation(GraphProperty.class)!=null) {
			GraphProperty graph = p.getAnnotation(GraphProperty.class);
			// Test added to avoid default value (which defaults name to "")
			if(graph.name()!=null && graph.name().length()>0)
				return graph.name();
		}
		if(p.getAnnotation(Column.class)!=null) {
			Column column = p.getAnnotation(Column.class);
			return column.name();
		}
		return getDefaultEdgeNameFor(p);
	}

	public static String getDefaultEdgeNameFor(Property p) {
		return p.getDeclaringClass().getName()+":"+p.getName();
	}

	/**
	 * Create a vertex out of a basic object. if object is of a simple type, we'll use value as id. Elsewhere, we will generate an id for object
	 * @param database database in which vertex will be stored
	 * @param value
	 * @return
	 */
	public static Vertex getVertexForLiteral(GraphDatabaseDriver database, Object value) {
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
	 * @param driver driver used to load data
	 * @param classLoader class loader used to find class
	 * @param key vertex containing object id
	 * @param property property used to navigate to this value. it allows disambiguation for literal values (which may be linked to more than one type, the typical example being
	 * a saved float, say "3.0", which may also be refered as the string "3.0").
	 * @param repository service repository, used to disambiguate subclass of literal and managed class
	 * @param objectsBeingAccessed 
	 * @return a fresh instance, with only id set
	 */
	public static Object createInstance(GraphDatabaseDriver driver, ClassLoader classLoader, Vertex key, Class<?> defaultType, ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		String effectiveType = null;
		Kind kind = getKindOf(key);
		if(Kind.literal==kind) {
			/* One literal node may be used according to different types. To disambiguate, we check if effective type matches default one. If not (typically 
			 * type returns string and user wants number), prefer default type.
			 */
			effectiveType = driver.getEffectiveType(key);
			try {
				if(!Collection.class.isAssignableFrom(defaultType) && !defaultType.isAssignableFrom(Class.forName(effectiveType))) {
					effectiveType = defaultType.getName();
				}
			} catch(Exception e) {
				// nothing to do : we use effective type - or try to
			}
		} else {
			effectiveType = driver.getEffectiveType(key);
		}
		if(classLoader==null) {
			throw new UnspecifiedClassLoader();
		}
		try {
			if(Literals.containsKey(classLoader, effectiveType) && !repository.containsKey(effectiveType)) {
				LiteralTransformer transformer = Literals.get(classLoader, effectiveType);
				return transformer.loadObject(driver, classLoader, effectiveType, key);
			} else {
				Class<?> type = classLoader.loadClass(effectiveType);
				if(Tuples.containsKey(type) && !repository.containsKey(type)) {
					// Tuples are handled the object way (easier, but more dangerous
					TupleTransformer transformer = Tuples.get(type);
					return transformer.loadObject(driver, classLoader, type, key, repository, objectsBeingAccessed);
				} else {
					return  type.newInstance();
				}
			}
		} catch(Exception e) {
			throw new UnableToCreateException(effectiveType, e);
		}
	}

	public static Kind getKindOf(Vertex key) {
		String kindName = key.getProperty(Properties.kind.name()).toString();
		Kind kind = Kind.valueOf(kindName);
		return kind;
	}

	/**
	 * get an id value for the given object whatever the object is
	 * @param repository
	 * @param value
	 * @return
	 */
	public static <DataType> String getIdOf(ServiceRepository repository, DataType value) {
		Class<? extends Object> valueClass = value.getClass();
		if(repository.containsKey(valueClass)) {
			IndexableGraphBackedFinderService<DataType, ?> service = (IndexableGraphBackedFinderService<DataType, ?>) repository.get(valueClass);
			// All ids are string, don't worry about it
			return service.getIdOf(value).toString();
		} else if(Literals.containsKey(valueClass)) {
			return getIdOfLiteral(valueClass, null, value);
		} else if(Tuples.containsKey(valueClass)) {
			return getIdOfTuple(repository, valueClass, value);
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
	public static String getIdOfLiteral(Class<?> declaredClass, Property idProperty, Object objectId) {
		PropertyMappingStrategy strategy = PropertyMappingStrategy.prefixed;
		if(idProperty!=null && idProperty.getAnnotation(GraphProperty.class)!=null) {
			strategy = idProperty.getAnnotation(GraphProperty.class).mapping();
		}
		return strategy.literalToId(declaredClass, idProperty, objectId);
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
	public static String getIdOfTuple(ServiceRepository repository, Class<?> declaredClass, Object value) {
		return Tuples.get(declaredClass).getIdOfTuple(repository, value);
	}

	/**
	 * Generates a vertex for the given tuple
	 * @param bluePrintsBackedFinderService source service, some informations may be extracted from it
	 * @param repository service repository for non literal values
	 * @param value tuple to persist
	 * @param objectsBeingUpdated map of objects already being accessed. Links object id to object
	 * @return the
	 */
	public static Vertex getVertexForTuple(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, ServiceRepository repository, Object value, Map<String, Object> objectsBeingUpdated) {
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
	 * Converts a vertex to a string by outputing all its properties values
	 * @param objectVertex
	 * @return
	 */
	public static String toString(Vertex objectVertex) {
		StringBuilder sOut = new StringBuilder("{");
		for(String s : objectVertex.getPropertyKeys()) {
			if(sOut.length()>1)
				sOut.append("; ");
			sOut.append(s).append("=").append(objectVertex.getProperty(s));
		}
		return sOut.append("}").toString();
	}
}
