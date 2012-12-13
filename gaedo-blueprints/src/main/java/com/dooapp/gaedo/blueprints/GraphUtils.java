package com.dooapp.gaedo.blueprints;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;

import com.dooapp.gaedo.blueprints.annotations.GraphProperty;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.UnableToGetVertexTypeException;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TupleTransformer;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class GraphUtils {
	private static final String GAEDO_PREFIX = "https://github.com/Riduidel/gaedo/";

	/**
	 * Ontologic context used by all gaedo graph elements.
	 */
	public static final String GAEDO_CONTEXT = GAEDO_PREFIX + "visible";

	/**
	 * Ontologic context used by gaedo graph elements that we want to keep
	 * hidden. Those elements should never be exported. To make sure this works
	 * well, this context is set to null. Crazy no ?
	 */
	public static final String GAEDO_HIDDEN_CONTEXT = GAEDO_PREFIX + "hidden";

	private static final Logger logger = Logger.getLogger(GraphUtils.class.getName());

	public static String asSailProperty(String context) {
		if (GraphSail.NULL_CONTEXT_NATIVE.equals(context))
			return context;
		return GraphSail.URI_PREFIX + " " + context;
	}

	/**
	 * Generate edge name from property infos. Notice generated edge name will
	 * first be searched in property annotations, and only if none compatile
	 * found by generating a basic property name
	 * 
	 * @param p
	 *            source property
	 * @return an edge name (by default property container class name + "." +
	 *         property name
	 */
	public static String getEdgeNameFor(Property p) {
		if (p.getAnnotation(GraphProperty.class) != null) {
			GraphProperty graph = p.getAnnotation(GraphProperty.class);
			// Test added to avoid default value (which defaults name to "")
			if (graph.name() != null && graph.name().trim().length() > 0)
				return graph.name();
		}
		if (p.getAnnotation(Column.class) != null) {
			Column column = p.getAnnotation(Column.class);
			if (column.name() != null && column.name().trim().length() > 0)
				return column.name();
		}
		return getDefaultEdgeNameFor(p);
	}

	public static String getDefaultEdgeNameFor(Property p) {
		return p.getDeclaringClass().getName() + ":" + p.getName();
	}

	/**
	 * Create a vertex out of a basic object. if object is of a simple type,
	 * we'll use value as id. Elsewhere, we will generate an id for object
	 * 
	 * @param database
	 *            database in which vertex will be stored
	 * @param value
	 * @return
	 */
	public static Vertex getVertexForLiteral(GraphDatabaseDriver database, Object value) {
		Vertex returned = null;
		// Now distinct behaviour between known objects and unknown ones
		Class<? extends Object> valueClass = value.getClass();
		if (Literals.containsKey(valueClass)) {
			LiteralTransformer transformer = Literals.get(valueClass);
			returned = transformer.getVertexFor(database, valueClass.cast(value));
		} else {
			throw new ObjectIsNotARealLiteralException(value, valueClass);
			// TODO do not forget to set id property
		}
		return returned;
	}

	/**
	 * Create an object instance from a literal vertex compatible with this
	 * service contained class
	 * 
	 * @param driver
	 *            driver used to load data
	 * @param strategy
	 *            TODO
	 * @param classLoader
	 *            class loader used to find class
	 * @param key
	 *            vertex containing object id
	 * @param repository
	 *            service repository, used to disambiguate subclass of literal
	 *            and managed class
	 * @param objectsBeingAccessed
	 * @param property
	 *            property used to navigate to this value. it allows
	 *            disambiguation for literal values (which may be linked to more
	 *            than one type, the typical example being a saved float, say
	 *            "3.0", which may also be refered as the string "3.0").
	 * @return a fresh instance, with only id set
	 */
	public static Object createInstance(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, Vertex key, Class<?> defaultType,
					ServiceRepository repository, Map<String, Object> objectsBeingAccessed) {
		String effectiveType = null;
		Kind kind = getKindOf(key);
		/*
		 * One literal node may be used according to different types. To
		 * disambiguate, we check if effective type matches default one. If not
		 * (typically type returns string and user wants number), prefer default
		 * type.
		 */
		try {
			effectiveType = driver.getEffectiveType(key);
		} catch (UnableToGetVertexTypeException untypedVertex) {
			try {
				// Don't remember the reason of that mess
				if (!Collection.class.isAssignableFrom(defaultType) && !defaultType.isAssignableFrom(Class.forName(effectiveType))) {
					effectiveType = defaultType.getName();
				}
			} catch (Exception unableToLoadClass) {
				// nothing to do : we use effective type - or try to
			}
			if (effectiveType == null) {
				// First alternative is here for untyped strings in uris nodes (like uris themselves when treated as strings)
				// Second alternative is there for cases when we try to load a collection of untyped thingies
				if (String.class.isAssignableFrom(defaultType) || Collection.class.isAssignableFrom(defaultType))
					effectiveType = GraphMappingStrategy.STRING_TYPE;

			}
		}
		if (classLoader == null) {
			throw new UnspecifiedClassLoader();
		}
		try {
			if (Literals.containsKey(classLoader, effectiveType) && !repository.containsKey(effectiveType)) {
				LiteralTransformer transformer = Literals.get(classLoader, effectiveType);
				return transformer.loadObject(driver, classLoader, effectiveType, key);
			} else {
				Class<?> type = classLoader.loadClass(effectiveType);
				if (Tuples.containsKey(type) && !repository.containsKey(type)) {
					// Tuples are handled the object way (easier, but more
					// dangerous
					TupleTransformer transformer = Tuples.get(type);
					return transformer.loadObject(driver, strategy, classLoader, type, key, repository, objectsBeingAccessed);
				} else {
					return type.newInstance();
				}
			}
		} catch (Exception e) {
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
	 * 
	 * @param repository
	 * @param value
	 * @return
	 */
	public static <DataType> String getIdOf(ServiceRepository repository, DataType value) {
		Class<? extends Object> valueClass = value.getClass();
		if (repository.containsKey(valueClass)) {
			AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, ?> service = (AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, ?>) repository
							.get(valueClass);
			// All ids are string, don't worry about it
			return service.getIdOf(value).toString();
		} else if (Literals.containsKey(valueClass)) {
			return getIdOfLiteral(valueClass, null, value);
		} else if (Tuples.containsKey(valueClass)) {
			return getIdOfTuple(repository, valueClass, value);
		} else {
			throw new ImpossibleToGetIdOfUnknownType(valueClass);
		}
	}

	/**
	 * Get the value of the vertex id for the given literal
	 * 
	 * @param database
	 *            used graph
	 * @param declaredClass
	 *            declared object class
	 * @param idProperty
	 *            gives the declared type of id (which may differ from primitive
	 *            types, where user may give an integer instead of a long, as an
	 *            example). Notice that, contrary to most of gaedo code, this
	 *            field can be null
	 * @param objectId
	 *            object id value
	 * @return the value used by {@link Properties#vertexId} to identify the
	 *         vertex associated to that object
	 */
	public static String getIdOfLiteral(Class<?> declaredClass, Property idProperty, Object objectId) {
		PropertyMappingStrategy strategy = PropertyMappingStrategy.prefixed;
		if (idProperty != null && idProperty.getAnnotation(GraphProperty.class) != null) {
			strategy = idProperty.getAnnotation(GraphProperty.class).mapping();
		}
		return strategy.literalToId(declaredClass, idProperty, objectId);
	}

	/**
	 * Get the value of the vertex id for the given object
	 * 
	 * @param database
	 *            used graph
	 * @param declaredClass
	 *            declared object class
	 * @param idProperty
	 *            gives the declared type of id (which may differ from primitive
	 *            types, where user may give an integer instead of a long, as an
	 *            example). Notice that, contrary to most of gaedo code, this
	 *            field can be null
	 * @param value
	 *            object id value
	 * @return the value used by {@link Properties#vertexId} to identify the
	 *         vertex associated to that object
	 */
	public static String getIdOfTuple(ServiceRepository repository, Class<?> declaredClass, Object value) {
		return Tuples.get(declaredClass).getIdOfTuple(repository, value);
	}

	/**
	 * Generates a vertex for the given tuple
	 * 
	 * @param bluePrintsBackedFinderService
	 *            source service, some informations may be extracted from it
	 * @param repository
	 *            service repository for non literal values
	 * @param value
	 *            tuple to persist
	 * @param objectsBeingUpdated
	 *            map of objects already being accessed. Links object id to
	 *            object
	 * @return the
	 */
	public static Vertex getVertexForTuple(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, ServiceRepository repository, Object value,
					Map<String, Object> objectsBeingUpdated) {
		Vertex returned = null;
		// Now distinct behaviour between known objects and unknown ones
		Class<? extends Object> valueClass = value.getClass();
		if (Tuples.containsKey(valueClass)) {
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
		if (returned.contains(CascadeType.ALL)) {
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
	 * 
	 * @param objectVertex
	 * @return
	 */
	public static String toString(Vertex objectVertex) {
		StringBuilder sOut = new StringBuilder("{");
		for (String s : objectVertex.getPropertyKeys()) {
			if (sOut.length() > 1)
				sOut.append("; ");
			sOut.append(s).append("=").append(objectVertex.getProperty(s));
		}
		return sOut.append("}").toString();
	}

	/**
	 * Find all contexts in given edge by looking, in
	 * {@link GraphSail#CONTEXT_PROP} property, what are the contexts. These
	 * contexts are extracted by iteratively calling {@link #CONTEXTS_MATCHER}
	 * and {@link Matcher#find(int)} method.
	 * 
	 * @param edge
	 *            input edge
	 * @return collection of declared contexts.
	 */
	public static Collection<String> getContextsOf(Edge edge) {
		String contextsString = edge.getProperty(GraphSail.CONTEXT_PROP).toString();
		Matcher matcher = CONTEXTS_MATCHER.matcher(contextsString);
		Collection<String> output = new LinkedList<String>();
		int character = 0;
		while (matcher.find(character)) {
			if (GraphSail.NULL_CONTEXT_NATIVE.equals(matcher.group(1))) {
				// the null context is a low-level view. It should be associated
				// with "no named graph" (that's to say an empty collection).
				return output;
			} else if (matcher.group(1).startsWith(GraphSail.URI_PREFIX + "")) {
				output.add(matcher.group(2));
			}
			character = matcher.end();
		}
		return output;
	}

	/**
	 * Compiled pattern used to match strings such as
	 * 
	 * <pre>
	 * U https://github.com/Riduidel/gaedo/visible  U http://purl.org/dc/elements/1.1/description
	 * </pre>
	 * 
	 * or
	 * 
	 * <pre>
	 * N U http://purl.org/dc/elements/1.1/description
	 * </pre>
	 * 
	 * or even
	 * 
	 * <pre>
	 * N
	 * </pre>
	 * 
	 * You know why I do such a pattern matching ? Because sail graph named
	 * graph definintion goes by concatenaing contexts URI in edges properties.
	 * This is really douchebag code !
	 */
	public static final Pattern CONTEXTS_MATCHER = Pattern.compile("(N|U ([\\S]+))+");

	/**
	 * Check if edge has the required named graphs list
	 * 
	 * @param e
	 *            edge to test
	 * @param namedGraphs
	 *            named graphs the edge must have
	 * @return true if edge contexts are the given collection of named graphs
	 */
	public static boolean isInNamedGraphs(Edge e, Collection<String> namedGraphs) {
		Collection<String> contexts = getContextsOf(e);
		// Only analyse edge if it is in named graph, and only in named graphs
		boolean isInNamedGraphs = contexts.size() == namedGraphs.size() && contexts.containsAll(namedGraphs);
		return isInNamedGraphs;
	}
}
