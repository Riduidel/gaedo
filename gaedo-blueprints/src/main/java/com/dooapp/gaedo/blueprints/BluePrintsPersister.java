package com.dooapp.gaedo.blueprints;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.dooapp.gaedo.blueprints.ObjectCache.ValueLoader;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

public class BluePrintsPersister {
    private static final Logger logger = Logger.getLogger(BluePrintsPersister.class.getName());

    public BluePrintsPersister(Kind node) {
    }


    /**
     * Create or update given object
     *
     * @param service             source of modification
     * @param objectVertexId      object expected vertex id
     * @param objectVertex        vertex corresponding to object to update
     * @param valueClass          class of the value to be updated here
     * @param containedProperties list of contained properties
     * @param toUpdate            object to update
     * @param cascade             kind of cascade used for dependent properties
     * @param objectsBeingUpdated map containing subgraph of obejcts currently being updated, this is used to avoid loops, and NOT as a cache
     * @return updated object
     */
    public <DataType> Object performUpdate(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, String objectVertexId, Vertex objectVertex, Class<?> valueClass, Map<Property, Collection<CascadeType>> containedProperties, Object toUpdate, CascadeType cascade, ObjectCache objectsBeingUpdated) {
        Graph database = service.getDatabase();
        // it's in fact an object creation
        if (objectVertex == null) {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "object " + objectVertexId.toString() + " has never before been seen in graph, so create central node for it");
            }
            objectVertex = service.getDriver().createEmptyVertex(valueClass, objectVertexId, toUpdate);
            // Create a value for that node (useful for RDF export)
            service.getDriver().setValue(objectVertex, objectVertexId);
        }
        // Here come the caching !
        DataType updated = (DataType) objectsBeingUpdated.get(objectVertexId);
        if (updated == null) {
            try {
                objectsBeingUpdated.put(objectVertexId, toUpdate);
                updateProperties(service, database, toUpdate, objectVertex, containedProperties, cascade, objectsBeingUpdated);
                return toUpdate;
            } finally {
                objectsBeingUpdated.remove(objectVertexId);
            }
        } else {
            return updated;
        }
    }

    /**
     * Delete given object
     *
     * @param service              source of modification
     * @param objectVertexId       object expected vertex id
     * @param objectVertex         vertex corresponding to object to delete
     * @param valueClass           class contained by service
     * @param containedProperties  list of contained properties
     * @param toDelete             object to delete
     * @param cascade              kind of cascade used for dependent properties
     * @param objectsBeingAccessed map containing subgraph of objects currently being delete, this is used to avoid loops, and NOT as a cache
     */
    public <DataType> void performDelete(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, String objectVertexId, Vertex objectVertex, Class<?> valueClass, Map<Property, Collection<CascadeType>> containedProperties, DataType toDelete, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        Graph database = service.getDatabase();
        for (Map.Entry<Property, Collection<CascadeType>> entry : containedProperties.entrySet()) {
        	Property p = entry.getKey();
            // Static properties are by design not written
            if (!p.hasModifier(Modifier.STATIC) && !Annotations.TRANSIENT.is(p)) {
                // Per default, no operation is cascaded
                CascadeType used = null;
                // However, if property supports that cascade type, we cascade operation
                if (entry.getValue().contains(cascade)) {
                    used = cascade;
                }
                if(used!=null) {
	                Class<?> rawPropertyType = p.getType();
	                Collection<CascadeType> toCascade = containedProperties.get(p);
	                if (Collection.class.isAssignableFrom(rawPropertyType)) {
	                    if (logger.isLoggable(Level.FINEST)) {
	                        logger.log(Level.FINEST, "property " + p.getName() + " is considered a collection one");
	                    }
	                    deleteCollection(service, database, p, toDelete, objectVertex, toCascade, objectsBeingAccessed);
	                    // each value should be written as an independant value
	                } else if (Map.class.isAssignableFrom(rawPropertyType)) {
	                    if (logger.isLoggable(Level.FINEST)) {
	                        logger.log(Level.FINEST, "property " + p.getName() + " is considered a map one");
	                    }
	                    deleteMap(service, database, p, toDelete, objectVertex, toCascade, objectsBeingAccessed);
	                } else {
	                    deleteSingle(service, database, p, toDelete, objectVertex, toCascade, objectsBeingAccessed);
	                }
                }
            }
        }
        /* We try to locate vertex in graph before to delete it. Indeed, mainly due cascade delete, this vertex may have already been removed */
        Vertex notYetDeleted = service.getDriver().loadVertexFor(objectVertexId, valueClass.getName());
        if(notYetDeleted!=null)
        	GraphUtils.removeSafely(database, notYetDeleted);
    }


    /**
     * Delete value mapped by a single property.
     * Value is deleted only if unchanged during the delete call (that to say value in object is the same that value in graph).
     * @param service
     * @param database
     * @param p
     * @param toDelete
     * @param objectVertex
     * @param toCascade
     * @param objectsBeingAccessed
     */
    private void deleteSingle(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Graph database, Property p, Object toDelete, Vertex objectVertex, Collection<CascadeType> toCascade, ObjectCache objectsBeingAccessed) {
        // there should be only one vertex to delete
        Iterable<Edge> edges = service.getStrategy().getOutEdgesFor(objectVertex, p);
        for (Edge e : edges) {
            Vertex valueVertex = e.getVertex(Direction.IN);
            GraphUtils.removeSafely(database, e);
            // Now what to do with vertex ? Delete it ?
            if (toCascade.contains(CascadeType.REMOVE)) {
                // yes, delete it forever (but before, see if there aren't more datas to delete
                Object value = p.get(toDelete);
                if(value!=null) {
                	Vertex knownValueVertex = service.getVertexFor(value, CascadeType.REFRESH, objectsBeingAccessed);
                	if(knownValueVertex!=null && knownValueVertex.equals(valueVertex))
                		service.deleteOutEdgeVertex(objectVertex, valueVertex, value, objectsBeingAccessed);
                }

            }
        }
    }

    /**
     * Delete values from a map.
     * Notice we only delete values which are mapped to vertices in graph that the edges corresponding to that property can lead us to.
     * @param service service used to map values to vertices
     * @param database database to remvoe vertices from
     * @param p property from which map is loaded
     * @param toDelete object we want to delete the map from
     * @param objectVertex vertex corresponding to toDelete
     * @param toCascade cascade mode
     * @param objectsBeingAccessed
     */
    private void deleteMap(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Graph database, Property p, Object toDelete, Vertex objectVertex, Collection<CascadeType> toCascade, ObjectCache objectsBeingAccessed) {
        Iterable<Edge> edges = service.getStrategy().getOutEdgesFor(objectVertex, p);
        Map<?, ?> values = (Map<?, ?>) p.get(toDelete);
        Map<Vertex, Edge> oldVertices = new HashMap<Vertex, Edge>();
        for (Edge e : edges) {
            Vertex inVertex = e.getVertex(Direction.IN);
            oldVertices.put(inVertex, e);
        }
        for (Object v : values.entrySet()) {
            Vertex valueVertex = service.getVertexFor(v, CascadeType.REFRESH, objectsBeingAccessed);
            if (valueVertex!=null && oldVertices.containsKey(valueVertex)) {
                Edge oldEdge = oldVertices.remove(valueVertex);
                GraphUtils.removeSafely(database, oldEdge);
                if (toCascade.contains(CascadeType.REMOVE)) {
                    service.deleteOutEdgeVertex(objectVertex, valueVertex, v, objectsBeingAccessed);
                }
            }
        }
        if (oldVertices.size() > 0) {
            // force deletion of remaining edges
            // BUT assocaited vertices may not be deleted
            for (Edge e : oldVertices.values()) {
                GraphUtils.removeSafely(database, e);
            }
        }
    }

    private void deleteCollection(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Graph database, Property p, Object toDelete, Vertex objectVertex, Collection<CascadeType> toCascade, ObjectCache objectsBeingAccessed) {
        Iterable<Edge> edges = service.getStrategy().getOutEdgesFor(objectVertex, p);
        Collection<?> values = (Collection<?>) p.get(toDelete);
        Map<Vertex, Edge> oldVertices = new HashMap<Vertex, Edge>();
        for (Edge e : edges) {
            Vertex inVertex = e.getVertex(Direction.IN);
            oldVertices.put(inVertex, e);
        }
        for (Object v : values) {
        	// already heard about null-containing collections ? I do know them, and they're pure EVIL
        	if(v!=null) {
	            Vertex valueVertex = service.getVertexFor(v, CascadeType.REFRESH, objectsBeingAccessed);
	            if (valueVertex !=null && oldVertices.containsKey(valueVertex)) {
	                Edge oldEdge = oldVertices.remove(valueVertex);
	                GraphUtils.removeSafely(database, oldEdge);
	                if (toCascade.contains(CascadeType.REMOVE)) {
	                    service.deleteOutEdgeVertex(objectVertex, valueVertex, v, objectsBeingAccessed);
	                }
	            }
        	}
        }
        if (oldVertices.size() > 0) {
            // force deletion of remaining edges
            // BUT assocaited vertices may not be deleted
            for (Edge e : oldVertices.values()) {
                GraphUtils.removeSafely(database, e);
            }
        }
    }

    /**
     * Update all properties of given object
     *
     * @param toUpdate             object to update
     * @param objectVertex         object root vertex
     * @param containedProperties  map linking each object property to the cascade types associated to it (allows us to easily see if there is any cascade to perform on object)
     * @param cascade              cascade type used to perform this operation, depend if this method is called from a {@link #create(Object)} or an {@link #update(Object)}
     * @param objectsBeingAccessed cache of objects being accessed during that write
     */
    private <DataType> void updateProperties(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Graph database, Object toUpdate, Vertex objectVertex, Map<Property, Collection<CascadeType>> containedProperties, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        for (Map.Entry<Property, Collection<CascadeType>> entry : containedProperties.entrySet()) {
            Property p = entry.getKey();
            // Static properties are by design not written
            if (!p.hasModifier(Modifier.STATIC) && !Annotations.TRANSIENT.is(p)) {
                // Per default, no operation is cascaded
                CascadeType used = null;
                // However, if property supports that cascade type, we cascade operation
                if (entry.getValue().contains(cascade)) {
                    used = cascade;
                }
                // We only perform operations on cascaded fields
                if(used!=null) {
	                Class<?> rawPropertyType = p.getType();
	                if (Collection.class.isAssignableFrom(rawPropertyType)) {
	                    if (logger.isLoggable(Level.FINEST)) {
	                        logger.log(Level.FINEST, "property " + p.getName() + " is considered a collection one");
	                    }
	                    updateCollection(service, database, p, toUpdate, objectVertex, used, objectsBeingAccessed);
	                    // each value should be written as an independant value
	                } else if (Map.class.isAssignableFrom(rawPropertyType)) {
	                    if (logger.isLoggable(Level.FINEST)) {
	                        logger.log(Level.FINEST, "property " + p.getName() + " is considered a map one");
	                    }
	                    updateMap(service, database, p, toUpdate, objectVertex, used, objectsBeingAccessed);
	                } else {
	                    updateSingle(service, database, p, toUpdate, objectVertex, used, objectsBeingAccessed);
	                }
                }
            }
        }
        // Migrator property has been added to object if needed
        // it's also the case of classes list
    }

    /**
     * Persisting a map consist into considering each map entry as an object of the map entries collection, then associating each entry object to its contained key and value.
     * To make this association as easy (and readable as posisble) map entries keys are their keys objects ids (if managed) or values) elsewhere, and values are
     * their values ids (if managed) or values (elsewhere). Notice a link is always made between a map entry and both its key and value.
     *
     * @param p          property containing that map
     * @param toUpdate   map to update
     * @param rootVertex object root vertex
     * @param cascade    used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
     */
    private <DataType> void updateMap(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Graph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        // Cast should work like a charm
        Map<?, ?> value = (Map<?, ?>) p.get(toUpdate);
        // As a convention, null values are never stored
        if (value != null /* && value.size()>0 that case precisely created https://github.com/Riduidel/gaedo/issues/13 */) {
            // Get previously existing vertices
            Iterable<Edge> existingIterator = service.getStrategy().getOutEdgesFor(rootVertex, p);
            // Do not change previously existing vertices if they correspond to new ones
            // Which is done in that call : as vertex is always looked up before creation, there is little duplication risk
            // or at last that risk should be covered by selected Blueprints implementation
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
                // TODO also remove map entry vertex assocaited edges
            }
            // And finally add new vertices
            for (Vertex newVertex : newVertices) {
                service.getDriver().createEdgeFor(rootVertex, newVertex, p);
            }
        }
    }

    /**
     * Update given collection by creating a set of edges/vertices for each element
     *
     * @param p          properties to update associated vertices for
     * @param toUpdate   source object to update
     * @param rootVertex vertex associated to toUpdate
     * @param cascade    used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
     * @category update
     */
    private <DataType> void updateCollection(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Graph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        // Cast should work like a charm
        Collection<?> value = (Collection<?>) p.get(toUpdate);
        // As a convention, null values are never stored
        if (value != null /* && value.size()>0 that case precisely created https://github.com/Riduidel/gaedo/issues/13 */) {
            // Get previously existing vertices
            Iterable<Edge> previousEdges = service.getStrategy().getOutEdgesFor(rootVertex, p);
            // Get the new, updated Collection of vertices (which is already sorted).
            Collection<Vertex> allVertices = createCollectionVerticesFor(service, value, cascade, objectsBeingAccessed);
            // Keep track of the edges that correspond to each vertex (for later ordering)...
            Map<Vertex, List<Edge>> savedEdges = new HashMap<Vertex, List<Edge>>();
            // ...and put the old, invalid vertices aside for later deletion.
            Set<Edge> edgesToRemove = new HashSet<Edge>();

            for (Edge e : previousEdges) {
            	Vertex inVertex = e.getVertex(Direction.IN);
                if (allVertices.contains(inVertex)) {
                	if(!savedEdges.containsKey(inVertex))
                		savedEdges.put(inVertex, new LinkedList<Edge>());
                    savedEdges.get(inVertex).add(e);
                } else {
                    edgesToRemove.add(e);
                }
            }

            // Delete the edges that we don't need anymore.
            for (Edge edge : edgesToRemove) {
            	GraphUtils.removeSafely(database, edge);
            }

            // Then, go through the updated Vertices. Create edges if necessary, then always set the order property.
            // This is possible since #createCollectionVerticesFor maintains the ordering.
            int order = 0;
            for (Vertex vertex : allVertices) {
            	Edge edgeForVertex;
            	if(savedEdges.containsKey(vertex)) {
            		List<Edge> edges = savedEdges.get(vertex);
            		edgeForVertex = edges.remove(0);
            		if(edges.size() == 0)
            			savedEdges.remove(vertex);
            	} else
            		edgeForVertex = service.getDriver().createEdgeFor(rootVertex, vertex, p);

                // Add a fancy-schmancy property to maintain order in this town
                edgeForVertex.setProperty(Properties.collection_index.name(), order++);
            }

            // Finally, delete any remaining edges.
            for(Vertex vertex : savedEdges.keySet())
            	for(Edge edge : savedEdges.get(vertex))
            		GraphUtils.removeSafely(database, edge);
        }
    }

    /**
     * Create a collection of vertices for the given collection of values
     *
     * @param value   collection of values to create vertices for
     * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
     * @return collection of vertices created by {@link #getVertexFor(Object)}. order of vertices is guaranteed to be the same as input value one.
     */
    private Collection<Vertex> createCollectionVerticesFor(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Collection<?> value, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        Collection<Vertex> returned = new java.util.LinkedList<Vertex>();
        for (Object o : value) {
        	// already heard about null-containing collections ? I do know them, and they're pure EVIL
        	if(o!=null)
        		returned.add(service.getVertexFor(o, cascade, objectsBeingAccessed));
        }
        return returned;
    }

    /**
     * Create a collection of map vertices (each representing one map entry) for each entry of the input map.
     *
     * @param value   map of values to create vertices for
     * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
     * @return collection of vertices created by {@link #getVertexFor(Object)}
     * @see #getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?>, CascadeType, Map) for details about the way to generate a vertex for a Map.Entry node
     */
    private Collection<Vertex> createMapVerticesFor(AbstractBluePrintsBackedFinderService<? extends Graph, ?, ?> service, Map value, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        Collection<Vertex> returned = new HashSet<Vertex>();
        // Strangely, the entrySet is not seen as a Set<Entry>
        for (Entry o : (Set<Entry>) value.entrySet()) {
            returned.add(service.getVertexFor(o, cascade, objectsBeingAccessed));
        }
        return returned;
    }

    /**
     * Update single-valued property by changing target of edge used to represent the property
     *
     * @param p          updated property
     * @param toUpdate   updated object
     * @param rootVertex vertex representing the object
     * @param cascade    used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
     * @category update
     */
    private <DataType> void updateSingle(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Graph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, ObjectCache objectsBeingAccessed) {
        Object value = p.get(toUpdate);
        // As a convention, null values are never stored but they may replace existing ones, in which case previous values must be removed
        // as a consequenc,v alueVertex is loaded only for non null values
        Vertex valueVertex = null;
        if (value != null) {
            valueVertex = service.getVertexFor(value, cascade, objectsBeingAccessed);
        }
        /* Totally crazy confident non-nullity lack of test :
         * this method is only called when cascade type is either PERSIST or MERGE. In both cases the call to getVertexFor will create the vertex if missing.
         * As a consequence there is no need for nullity check.
         */
        Edge link = null;
        // Get previously existing vertex
        List<Edge> matching = CollectionUtils.asList(service.getStrategy().getOutEdgesFor(rootVertex, p));
        // property is single-valued, so iteration can be done at most one
        if (matching.size()==1) {
            // There is an existing edge, change its target and maybe delete previous one
            Edge existing = matching.get(0);
            if (valueVertex != null && existing.getVertex(Direction.IN).equals(valueVertex)) {
                // Nothing to do
                link = existing;
            } else {
                // delete old edge (if it exists)
            	GraphUtils.removeSafely(database, existing);
                if (value != null)
                    link = service.getDriver().createEdgeFor(rootVertex, valueVertex, p);
            }
        } else if (matching.size()>1) {
            if (logger.isLoggable(Level.SEVERE)) {
                // There is some incoherent data in graph .. log it !
                StringBuilder sOut = new StringBuilder("An object with the following monovalued property\n").append(p.toGenericString()).append(" is linked to more than one vertex :");
                for(Edge e : matching) {
                    sOut.append("\n\t").append(e.getVertex(Direction.IN).toString());
                }
                logger.log(Level.SEVERE, "Graph contains some incoherence :" + sOut.toString());
            }
            // absolutly all edges are removed, including the first one. As a consequence, initial edge will have to be re-created
            for(Edge e : matching) {
            	GraphUtils.removeSafely(database, e);
            }
        }
        if (link == null && value != null)
            link = service.getDriver().createEdgeFor(rootVertex, valueVertex, p);
    }

    public <DataType> DataType loadObject(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
        String objectVertexId = service.getDriver().getIdOf(objectVertex);
        return loadObject(service, objectVertexId, objectVertex, objectsBeingAccessed);
    }

    /**
     * Load object with given vertex id and vertex node
     *
     * @param objectVertexId
     * @param objectVertex
     * @param objectsBeingAccessed map of objects currently being accessed, it avoid some loops during loading, but is absolutely NOT a persistent cache
     * @return loaded object
     */
    public <DataType> DataType loadObject(final AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, final String objectVertexId, final Vertex objectVertex, final ObjectCache objectsBeingAccessed) {
    	ValueLoader loader = new ValueLoader() {

			@Override
			public Object get() {
		        // Shortcut
		        if (objectVertex == null) {
		            objectsBeingAccessed.put(objectVertexId, null);
		            return null;
		        } else {
		            ClassLoader classLoader = service.getContainedClass().getClassLoader();
		            ServiceRepository repository = service.getRepository();
		            DataType returned = (DataType) GraphUtils.createInstance(service.getDriver(), service.getStrategy(), classLoader, objectVertex, Object.class /* we use object here, as this default type should not be used */, repository, objectsBeingAccessed);
		            try {
		                if (service.getStrategy().shouldLoadPropertiesOf(objectVertexId, objectVertex, objectsBeingAccessed)) {
		                    Map<Property, Collection<CascadeType>> containedProperties = service.getStrategy().getContainedProperties(returned, objectVertex, CascadeType.MERGE);
		                    objectsBeingAccessed.put(objectVertexId, returned);
		                    loadObjectProperties(service.getDriver(), service.getStrategy(), classLoader, repository, objectVertex, returned, containedProperties, objectsBeingAccessed);
		                }
		                return returned;
		            } finally {
		                // make sure loading call is always called, even if something failed during loading
		                service.getStrategy().loaded(objectVertexId, objectVertex, returned, objectsBeingAccessed);
//						objectsBeingAccessed.remove(objectVertexId);
		            }
		        }
			}

    	};
    	return (DataType) objectsBeingAccessed.get(objectVertexId, loader);
    }


    public <DataType> void loadObjectProperties(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, ServiceRepository repository, Vertex objectVertex,
                                                DataType returned, Map<Property, Collection<CascadeType>> containedProperties, ObjectCache objectsBeingAccessed) {
        for (Property p : containedProperties.keySet()) {
            if (!p.hasModifier(Modifier.STATIC) && !Annotations.TRANSIENT.is(p)) {
                Class<?> rawPropertyType = p.getType();
                if (Collection.class.isAssignableFrom(rawPropertyType)) {
                    loadCollection(driver, strategy, classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
                    // each value should be written as an independant value
                } else if (Map.class.isAssignableFrom(rawPropertyType)) {
                    loadMap(driver, strategy, classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
                } else {
                    loadSingle(driver, strategy, classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
                }
            }
        }
    }

    /**
     * Implementation tied to the future implementation of {@link #updateMap(Property, Object, Vertex, CascadeType)}
     *
     * @param strategy     TODO
     * @param p
     * @param returned
     * @param objectVertex
     */
    private <DataType> void loadMap(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, ServiceRepository repository, Property p, DataType returned, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
        boolean eagerLoad = false;
        // property may be associated to a onetomany or manytomany mapping. in such a case, check if there is an eager loading info
        OneToMany oneToMany = p.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            eagerLoad = FetchType.EAGER.equals(oneToMany.fetch());
        }
        if (!eagerLoad) {
            ManyToMany manyToMany = p.getAnnotation(ManyToMany.class);
            if (manyToMany != null) {
                eagerLoad = FetchType.EAGER.equals(manyToMany.fetch());
            }
        }
        Map<Object, Object> generatedCollection = (Map<Object, Object>) Utils.generateMap((Class<?>) p.getType(), null);
        MapLazyLoader handler = new MapLazyLoader(driver, strategy, classLoader, repository, p, objectVertex, generatedCollection, objectsBeingAccessed);
        if (eagerLoad) {
            handler.loadMap(generatedCollection, objectsBeingAccessed);
            p.set(returned, generatedCollection);
        } else {
            // Java proxy code
            p.set(returned, Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{p.getType(), Serializable.class, WriteReplaceable.class},
                    handler));
        }
    }

    /**
     * Load a single-valued property from graph
     *
     * @param strategy             TODO
     * @param p
     * @param returned
     * @param objectVertex
     * @param objectsBeingAccessed
     */
    private <DataType> void loadSingle(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classloader, ServiceRepository repository, Property p, DataType returned, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
        Iterator<Edge> iterator = strategy.getOutEdgesFor(objectVertex, p).iterator();
        if (iterator.hasNext()) {
            // yeah, there is a value !
            Edge edge = iterator.next();
            Vertex firstVertex = edge.getVertex(Direction.IN);
            Object value = GraphUtils.createInstance(driver, strategy, classloader, firstVertex, p.getType(), repository, objectsBeingAccessed);
            if (repository.containsKey(value.getClass())) {
                // value requires fields loading
                AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, ?> blueprints = (AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, ?>) repository.get(value.getClass());
                value = loadObject(blueprints, firstVertex, objectsBeingAccessed);
            }
            p.set(returned, value);
        }
        // TODO test unsupported multi-values
    }


    /**
     * Load collection corresponding to the given property for the given vertex.
     * BEWARE : here be lazy loading !
     *
     * @param strategy     TODO
     * @param p
     * @param returned
     * @param objectVertex
     */
    private <DataType> void loadCollection(
	    	GraphDatabaseDriver driver,
	    	GraphMappingStrategy strategy,
	    	ClassLoader classLoader,
	    	ServiceRepository repository,
	    	Property p,
	    	DataType returned,
	    	Vertex objectVertex,
	    	ObjectCache objectsBeingAccessed) {

    	// Figure out whether we want to lazy- or eager-load

        boolean eagerLoad = false;
        // property may be associated with a one-to-many or many-to-many mapping. in such a case, check if
        // there is an eager loading info
        OneToMany oneToMany = p.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            eagerLoad = FetchType.EAGER.equals(oneToMany.fetch());
        }
        if (!eagerLoad) {
            ManyToMany manyToMany = p.getAnnotation(ManyToMany.class);
            if (manyToMany != null) {
                eagerLoad = FetchType.EAGER.equals(manyToMany.fetch());
            }
        }

        // Get down to brass tacks

        Collection<Object> generatedCollection = Utils.generateCollection((Class<?>) p.getType(), null);
        CollectionLazyLoader handler = new CollectionLazyLoader(driver, strategy, classLoader, repository, p, objectVertex, generatedCollection, objectsBeingAccessed);
        if (eagerLoad) {
            handler.loadCollection(generatedCollection, objectsBeingAccessed);
            p.set(returned, generatedCollection);
        } else {
            // Java proxy code
            p.set(returned, Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{p.getType(), Serializable.class, WriteReplaceable.class},
                    handler));
        }
    }
}
