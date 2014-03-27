package com.dooapp.gaedo.blueprints.operations;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.CantCreateAVertexForALiteralException;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.transformers.LiteralHelper;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class Updater {
	public class UpdateProperties<DataType> extends AbstractCardinalityDistinguishingOperation {
		private final GraphDatabaseDriver driver;
		private final Graph database;
		private final AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service;
		private final ObjectCache objectsBeingAccessed;
		private final Object toUpdate;
		private final Vertex objectVertex;

		private UpdateProperties(GraphDatabaseDriver driver, Graph database, AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service,
						ObjectCache objectsBeingAccessed, Object toUpdate, Vertex objectVertex) {
			this.driver = driver;
			this.database = database;
			this.service = service;
			this.objectsBeingAccessed = objectsBeingAccessed;
			this.toUpdate = toUpdate;
			this.objectVertex = objectVertex;
		}

		@Override
		protected void operateOnSingle(Property p, CascadeType cascade) {
			updateSingle(service, driver, database, p, toUpdate, objectVertex, cascade, objectsBeingAccessed);
		}

		@Override
		protected void operateOnMap(Property p, CascadeType cascade) {
			updateMap(service, driver, database, p, toUpdate, objectVertex, cascade, objectsBeingAccessed);
		}

		@Override
		protected void operateOnCollection(Property p, CascadeType cascade) {
			updateCollection(service, driver, database, p, toUpdate, objectVertex, cascade, objectsBeingAccessed);
		}
	}

	public static final Boolean ELEMENT_IN_COLLECTION_MARKER = Boolean.TRUE;
	public static final String ELEMENT_IN_COLLECTION_MARKER_GRAPH_VALUE = Literals.get(ELEMENT_IN_COLLECTION_MARKER.getClass()).toString(ELEMENT_IN_COLLECTION_MARKER);

	private static final Logger logger = Logger.getLogger(Updater.class.getName());

	/**
	 * Create or update given object
	 *
	 * @param service
	 *            source of modification
	 * @param driver
	 *            TODO
	 * @param objectVertexId
	 *            object expected vertex id
	 * @param objectVertex
	 *            vertex corresponding to object to update
	 * @param valueClass
	 *            class of the value to be updated here
	 * @param containedProperties
	 *            list of contained properties
	 * @param toUpdate
	 *            object to update
	 * @param cascade
	 *            kind of cascade used for dependent properties
	 * @param objectsBeingUpdated
	 *            map containing subgraph of obejcts currently being updated,
	 *            this is used to avoid loops, and NOT as a cache
	 * @return updated object
	 */
	public <DataType> Object performUpdate(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, GraphDatabaseDriver driver,
					String objectVertexId, Vertex objectVertex, Class<?> valueClass, Map<Property, Collection<CascadeType>> containedProperties,
					Object toUpdate, CascadeType cascade, ObjectCache objectsBeingUpdated) {
		Graph database = service.getDatabase();
		// it's in fact an object creation
		if (objectVertex == null) {
			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER, "object " + objectVertexId.toString() + " has never before been seen in graph, so create central node for it");
			}
			objectVertex = driver.createEmptyVertex(valueClass, objectVertexId, toUpdate);
			// Create a value for that node (useful for RDF export)
			driver.setValue(objectVertex, objectVertexId);
		}
		// Here come the caching !
		DataType updated = (DataType) objectsBeingUpdated.get(objectVertexId);
		if (updated == null) {
			try {
				objectsBeingUpdated.put(objectVertexId, toUpdate);
				updateProperties(service, driver, database, toUpdate, objectVertex, containedProperties, cascade, objectsBeingUpdated);
				return toUpdate;
			} finally {
				objectsBeingUpdated.remove(objectVertexId);
			}
		} else {
			return updated;
		}
	}

	/**
	 * Update all properties of given object
	 *
	 * @param driver
	 *            TODO
	 * @param toUpdate
	 *            object to update
	 * @param objectVertex
	 *            object root vertex
	 * @param containedProperties
	 *            map linking each object property to the cascade types
	 *            associated to it (allows us to easily see if there is any
	 *            cascade to perform on object)
	 * @param cascade
	 *            cascade type used to perform this operation, depend if this
	 *            method is called from a {@link #create(Object)} or an
	 *            {@link #update(Object)}
	 * @param objectsBeingAccessed
	 *            cache of objects being accessed during that write
	 */
	private <DataType> void updateProperties(
					final AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service,
					final GraphDatabaseDriver driver,
					final Graph database,
					final Object toUpdate,
					final Vertex objectVertex,
					Map<Property, Collection<CascadeType>> containedProperties,
					CascadeType cascade,
					final ObjectCache objectsBeingAccessed) {
		new OperateOnProperties().execute(containedProperties, cascade, new UpdateProperties(driver, database, service, objectsBeingAccessed, toUpdate, objectVertex));
		// Migrator property has been added to object if needed
		// it's also the case of classes list
	}

	/**
	 * Persisting a map consist into considering each map entry as an object of
	 * the map entries collection, then associating each entry object to its
	 * contained key and value. To make this association as easy (and readable
	 * as posisble) map entries keys are their keys objects ids (if managed) or
	 * values) elsewhere, and values are their values ids (if managed) or values
	 * (elsewhere). Notice a link is always made between a map entry and both
	 * its key and value.
	 *
	 * @param driver
	 *            TODO
	 * @param p
	 *            property containing that map
	 * @param toUpdate
	 *            map to update
	 * @param rootVertex
	 *            object root vertex
	 * @param cascade
	 *            used cascade type, can be either {@link CascadeType#PERSIST}
	 *            or {@link CascadeType#MERGE}
	 */
	private <DataType> void updateMap(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, GraphDatabaseDriver driver, Graph database,
					Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, ObjectCache objectsBeingAccessed) {
		// Cast should work like a charm
		Map<?, ?> value = (Map<?, ?>) p.get(toUpdate);
		// As a convention, null values are never stored
		if (value != null /*
						 * && value.size()>0 that case precisely created
						 * https://github.com/Riduidel/gaedo/issues/13
						 */) {
			// Get previously existing vertices
			Iterable<Edge> existingIterator = service.getStrategy().getOutEdgesFor(rootVertex, p);
			// Do not change previously existing vertices if they correspond to
			// new ones
			// Which is done in that call : as vertex is always looked up before
			// creation, there is little duplication risk
			// or at last that risk should be covered by selected Blueprints
			// implementation
			Collection<Vertex> newVertices = createMapVerticesFor(service, value, cascade, objectsBeingAccessed);
			Map<Vertex, Edge> oldVertices = new HashMap<Vertex, Edge>();
			for (Edge e : existingIterator) {
				Vertex inVertex = e.getVertex(Direction.IN);
				if (newVertices.contains(inVertex)) {
					newVertices.remove(inVertex);
				} else {
					oldVertices.put(inVertex, e);
				}
			}
			// Now the have been collected, remove all old vertices
			for (Map.Entry<Vertex, Edge> entry : oldVertices.entrySet()) {
				GraphUtils.removeSafely(database, entry.getValue());
				// TODO also remove map entry vertex associated edges
			}
			// And finally add new vertices
			for (Vertex newVertex : newVertices) {
				driver.createEdgeFor(rootVertex, newVertex, p);
			}
		}
	}

	/**
	 * Update given collection by creating a set of edges/vertices for each
	 * element
	 *
	 * @param driver
	 *            TODO
	 * @param p
	 *            properties to update associated vertices for
	 * @param toUpdate
	 *            source object to update
	 * @param rootVertex
	 *            vertex associated to toUpdate
	 * @param cascade
	 *            used cascade type, can be either {@link CascadeType#PERSIST}
	 *            or {@link CascadeType#MERGE}
	 *
	 * @category update
	 */
	private <DataType> void updateCollection(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, GraphDatabaseDriver driver,
					Graph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, ObjectCache objectsBeingAccessed) {
		// Cast should work like a charm
		Collection<?> value = (Collection<?>) p.get(toUpdate);
		// As a convention, null values are never stored
		if (value != null /*
						 * && value.size()>0 that case precisely created
						 * https://github.com/Riduidel/gaedo/issues/13
						 */) {
			// Get previously existing vertices
			Iterable<Edge> previousEdges = service.getStrategy().getOutEdgesFor(rootVertex, p);
			// Get the new, updated Collection of vertices (which is already
			// sorted).
			Map<Object, Vertex> allVertices = createCollectionVerticesFor(service, value, cascade, objectsBeingAccessed);
			// Keep track of the edges that correspond to each vertex (for later
			// ordering)...
			Map<Vertex, List<Edge>> savedEdges = new HashMap<Vertex, List<Edge>>();
			// ...and put the old, invalid vertices aside for later deletion.
			Set<Edge> edgesToRemove = new HashSet<Edge>();

			// Add previous edges
			for (Edge e : previousEdges) {
				Vertex inVertex = e.getVertex(Direction.IN);
				if (allVertices.values().contains(inVertex)) {
					if (!savedEdges.containsKey(inVertex))
						savedEdges.put(inVertex, new LinkedList<Edge>());
					savedEdges.get(inVertex).add(e);
				} else {
					edgesToRemove.add(e);
				}
			}
			// And previous properties
			String propertyNamePrefix = GraphUtils.getEdgeNameFor(p);
			Collection<String> suspectProperties = new TreeSet<String>();
			for(String propertyName : rootVertex.getPropertyKeys()) {
				if(propertyName.startsWith(propertyNamePrefix)) {
					suspectProperties.add(propertyName);
				}
			}

			// Delete the edges that we don't need anymore.
			for (Edge edge : edgesToRemove) {
				GraphUtils.removeSafely(database, edge);
			}

			// Then, go through the updated Vertices. Create edges if necessary,
			// then always set the order property.
			// This is possible since #createCollectionVerticesFor maintains the
			// ordering.
			int order = 0;
			for(Object element : value) {
				if(allVertices.containsKey(element)) {
					Vertex vertex = allVertices.get(element);
					Edge edgeForVertex;
					if (savedEdges.containsKey(vertex)) {
						List<Edge> edges = savedEdges.get(vertex);
						edgeForVertex = edges.remove(0);
						if (edges.size() == 0)
							savedEdges.remove(vertex);
					} else
						edgeForVertex = driver.createEdgeFor(rootVertex, vertex, p);

					// Add a fancy-schmancy property to maintain order in this town (this property is NOT indexed)
					edgeForVertex.setProperty(Properties.collection_index.name(), order++);
				} else {
					// Element is a literal value, so it won't require a vertex, but a literal saving
					AbstractPropertyAdapter elementByIndexProperty = new LiteralInCollectionUpdaterProperty(p, order, element);
					updateLiteralPropertyIn(service.getDatabase(), toUpdate, rootVertex, elementByIndexProperty, element);
					suspectProperties.remove(GraphUtils.getEdgeNameFor(elementByIndexProperty));
					// We also add an inverted property allowing fast query of containing collection
					// Value here is not signifiant : we only want to mark collection as containing value. And for that, one simple string is enough
					AbstractPropertyAdapter elementByValueProperty = new LiteralInCollectionUpdaterProperty(p, element, ELEMENT_IN_COLLECTION_MARKER);
					elementByValueProperty.setGenericType(ELEMENT_IN_COLLECTION_MARKER.getClass());
					updateLiteralPropertyIn(service.getDatabase(), toUpdate, rootVertex, elementByValueProperty, ELEMENT_IN_COLLECTION_MARKER);
					suspectProperties.remove(GraphUtils.getEdgeNameFor(elementByValueProperty));
					order++;
				}
			}

			// Finally, delete any remaining edges.
			for (Vertex vertex : savedEdges.keySet())
				for (Edge edge : savedEdges.get(vertex))
					GraphUtils.removeSafely(database, edge);
			// and remaining suspect properties
			for(String suspect : suspectProperties) {
				rootVertex.removeProperty(suspect);
			}
			// and don't forget to write collection size property, for easier querying
			CollectionSizeProperty sizeProperty = new CollectionSizeProperty(p);
			updateLiteralPropertyIn(service.getDatabase(), toUpdate, rootVertex, sizeProperty, value.size());
		}
	}

	/**
	 * Create a collection of vertices for the given collection of values
	 *
	 * @param value
	 *            collection of values to create vertices for
	 * @param cascade
	 *            used cascade type, can be either {@link CascadeType#PERSIST}
	 *            or {@link CascadeType#MERGE}
	 * @return map linking objects to vertices created by {@link #getVertexFor(Object)}.
	 *         order of vertices is guaranteed to be the same as input value
	 *         one.
	 */
	private Map<Object, Vertex> createCollectionVerticesFor(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Collection<?> value,
					CascadeType cascade, ObjectCache objectsBeingAccessed) {
		Map<Object, Vertex> returned = new LinkedHashMap<Object, Vertex>();
		for (Object o : value) {
			// already heard about null-containing collections ? I do know them,
			// and they're pure EVIL
			if (o != null) {
				try {
					returned.put(o, service.getVertexFor(o, cascade, objectsBeingAccessed));
				} catch(CantCreateAVertexForALiteralException e) {
					// Literal objects have no vertices created for them. They
					// will be stored as "special" property values, which may
					// put a mess in many code blocks
				}
			}
		}
		return returned;
	}

	/**
	 * Create a collection of map vertices (each representing one map entry) for
	 * each entry of the input map.
	 *
	 * @param value
	 *            map of values to create vertices for
	 * @param cascade
	 *            used cascade type, can be either {@link CascadeType#PERSIST}
	 *            or {@link CascadeType#MERGE}
	 * @return collection of vertices created by {@link #getVertexFor(Object)}
	 * @see #getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph,
	 *      DataType, ?>, CascadeType, Map) for details about the way to
	 *      generate a vertex for a Map.Entry node
	 */
	private Collection<Vertex> createMapVerticesFor(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Map value, CascadeType cascade,
					ObjectCache objectsBeingAccessed) {
		Collection<Vertex> returned = new HashSet<Vertex>();
		// Strangely, the entrySet is not seen as a Set<Entry>
		for (Entry o : (Set<Entry>) value.entrySet()) {
			Vertex newVertex = service.getVertexFor(o, cascade, objectsBeingAccessed);
			returned.add(newVertex);
		}
		return returned;
	}

	/**
	 * Update single-valued property by changing target of edge used to
	 * represent the property
	 *
	 * @param driver
	 *            TODO
	 * @param p
	 *            updated property
	 * @param toUpdate
	 *            updated object
	 * @param rootVertex
	 *            vertex representing the object
	 * @param cascade
	 *            used cascade type, can be either {@link CascadeType#PERSIST}
	 *            or {@link CascadeType#MERGE}
	 *
	 * @category update
	 */
	private <DataType> void updateSingle(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, GraphDatabaseDriver driver,
					Graph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, ObjectCache objectsBeingAccessed) {
		Object value = p.get(toUpdate);
		// As a convention, null values are never stored but they may replace
		// existing ones, in which case previous values must be removed
		// as a consequence, valueVertex is loaded only for non null values
		Vertex valueVertex = null;
		if (value != null) {
			if (Literals.containsKey(value.getClass())) {
				updateLiteralPropertyIn(service.getDatabase(), toUpdate, rootVertex, p, value);
				return;
			} else {
				valueVertex = service.getVertexFor(value, cascade, objectsBeingAccessed);
			}
		}
		/*
		 * If vertex has a property named from the property name, remove it : it
		 * is due to a previous call to update with a literal non null property
		 * value, which may no more be the case if property type is ... Object
		 * (which is a bad idea)
		 */
		if (rootVertex.getPropertyKeys().contains(GraphUtils.getEdgeNameFor(p))) {
			rootVertex.removeProperty(GraphUtils.getEdgeNameFor(p));
		}
		/*
		 * Totally crazy confident non-nullity lack of test : this method is
		 * only called when cascade type is either PERSIST or MERGE. In both
		 * cases the call to getVertexFor will create the vertex if missing. As
		 * a consequence there is no need for nullity check.
		 */
		Edge link = null;
		// Get previously existing vertex
		List<Edge> matching = CollectionUtils.asList(service.getStrategy().getOutEdgesFor(rootVertex, p));
		// property is single-valued, so iteration can be done at most one
		if (matching.size() == 1) {
			// There is an existing edge, change its target and maybe delete
			// previous one
			Edge existing = matching.get(0);
			if (valueVertex != null && existing.getVertex(Direction.IN).equals(valueVertex)) {
				// Nothing to do
				link = existing;
			} else {
				// delete old edge (if it exists)
				GraphUtils.removeSafely(database, existing);
				if (value != null)
					link = driver.createEdgeFor(rootVertex, valueVertex, p);
			}
		} else if (matching.size() > 1) {
			if (logger.isLoggable(Level.SEVERE)) {
				// There is some incoherent data in graph .. log it !
				StringBuilder sOut = new StringBuilder("An object with the following monovalued property\n").append(p.toGenericString()).append(
								" is linked to more than one vertex :");
				for (Edge e : matching) {
					sOut.append("\n\t").append(e.getVertex(Direction.IN).toString());
				}
				logger.log(Level.SEVERE, "Graph contains some incoherence :" + sOut.toString());
			}
			// absolutly all edges are removed, including the first one. As a
			// consequence, initial edge will have to be re-created
			for (Edge e : matching) {
				GraphUtils.removeSafely(database, e);
			}
		}
		if (link == null && value != null)
			link = driver.createEdgeFor(rootVertex, valueVertex, p);
	}

	public void updateLiteralPropertyIn(Graph database, Object toUpdate, Vertex vertexToUpdate, Property property, Object propertyValue) {
		Class propertyClass = property.getType();
		GraphUtils.setIndexedProperty(database, vertexToUpdate, GraphUtils.getEdgeNameFor(property),
						LiteralHelper.getLiteralTextFor(propertyClass, propertyValue));
	}
}
