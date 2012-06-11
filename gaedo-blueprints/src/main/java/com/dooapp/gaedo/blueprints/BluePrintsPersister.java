package com.dooapp.gaedo.blueprints;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

public class BluePrintsPersister {
	private static final Logger logger = Logger.getLogger(BluePrintsPersister.class.getName());
	/**
	 * Node kind used by this updater
	 */
	private Kind nodeKind;
	
	public BluePrintsPersister(Kind node) {
		nodeKind = node;
	}


	/**
	 * Create or update given object
	 * @param service source of modification
	 * @param objectVertexId object expected vertex id
	 * @param valueClass TODO
	 * @param containedProperties list of contained properties
	 * @param toUpdate object to update
	 * @param cascade kind of cascade used for dependent properties
	 * @param objectsBeingUpdated map containing subgraph of obejcts currently being updated, this is used to avoid loops, and NOT as a cache
	 * @return updated object
	 */
	public <DataType> Object performUpdate(BluePrintsBackedFinderService<DataType, ?> service, String objectVertexId, Class<?> valueClass, Map<Property, Collection<CascadeType>> containedProperties, Object toUpdate, CascadeType cascade, Map<String, Object> objectsBeingUpdated) {
		IndexableGraph database = service.getDatabase();
		Vertex objectVertex = GraphUtils.locateVertex(database, Properties.vertexId, objectVertexId);
		// it's in fact an object creation
		if(objectVertex==null) {
			if (logger.isLoggable(Level.FINER)) {
				logger.log(Level.FINER, "object "+objectVertexId.toString()+" has never before been seen in graph, so create central node for it");
			}
			objectVertex = createIdVertex(database, valueClass, objectVertexId);
		}
		// Here come the caching !
		DataType updated = (DataType) objectsBeingUpdated.get(objectVertexId); 
		if(updated==null) {
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
	 * Creates the given id vertex
	 * @param database
	 * @param valueClass
	 * @param objectVertexId
	 * @return
	 */
	public Vertex createIdVertex(IndexableGraph database, Class<?> valueClass, String objectVertexId) {
		Vertex objectVertex;
		objectVertex = database.addVertex(objectVertexId);
		// As an aside, we add some indications regarding object id
		objectVertex.setProperty(Properties.vertexId.name(), objectVertexId);
		objectVertex.setProperty(Properties.kind.name(), nodeKind.name());
		objectVertex.setProperty(Properties.type.name(), valueClass.getName());
		return objectVertex;
	}

	/**
	 * Update all properties of given object
	 * @param toUpdate object to update
	 * @param objectVertex object root vertex
	 * @param containedProperties map linking each object property to the cascade types associated to it (allows us to easily see if there is any cascade to perform on object)
	 * @param cascade cascade type used to perform this operation, depend if this method is called from a {@link #create(Object)} or an {@link #update(Object)}
	 * @param objectsBeingAccessed cache of objects being accessed during that write
	 */
	private <DataType> void updateProperties(BluePrintsBackedFinderService<DataType, ?> service, IndexableGraph database, Object toUpdate, Vertex objectVertex, Map<Property, Collection<CascadeType>> containedProperties, CascadeType cascade, Map<String, Object> objectsBeingAccessed) {
		for(Map.Entry<Property, Collection<CascadeType>> entry : containedProperties.entrySet()) {
			Property p = entry.getKey();
			// Static properties are by design not written
			if(!p.hasModifier(Modifier.STATIC) && !Annotations.TRANSIENT.is(p)) {
				Class<?> rawPropertyType = p.getType();
				// Per default, no operation is cascaded
				CascadeType used = null;
				// However, if property supports that cascade type, we cascade operation
				if(entry.getValue().contains(cascade)) {
					used = cascade;
				}
				if(Collection.class.isAssignableFrom(rawPropertyType)) {
					if (logger.isLoggable(Level.FINEST)) {
						logger.log(Level.FINEST, "property "+p.getName()+" is considered a collection one");
					}
					updateCollection(service, database, p, toUpdate, objectVertex, cascade, objectsBeingAccessed);
					// each value should be written as an independant value
				} else if(Map.class.isAssignableFrom(rawPropertyType)) {
					if (logger.isLoggable(Level.FINEST)) {
						logger.log(Level.FINEST, "property "+p.getName()+" is considered a map one");
					}
					updateMap(service, database, p, toUpdate, objectVertex, cascade, objectsBeingAccessed);
				} else {
					updateSingle(service, database, p, toUpdate, objectVertex, cascade, objectsBeingAccessed);
				}
			}
		}
		// Migrator property has been added to object if needed
		// it's also the case of classes list
	}

	/**
	 * Create collection of entry vertices.
	 * Those vertices are very specific : they are not managed, but not really literal either
	 * @param value
	 * @return
	 */
	private Collection<Vertex> createVerticesFor(Map value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+BluePrintsBackedFinderService.class.getName()+"#createVerticesFor has not yet been implemented AT ALL");
	}
	
	/**
	 * Persisting a map consist into considering each map entry as an object of the map entries collection, then associating each entry object to its contained key and value.
	 * To make this association as easy (and readable as posisble) map entries keys are their keys objects ids (if managed) or values) elsewhere, and values are 
	 * their values ids (if managed) or values (elsewhere). Notice a link is always made between a map entry and both its key and value.
	 * @param p property containing that map
	 * @param toUpdate map to update
	 * @param rootVertex object root vertex
	 * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
	 */
	private <DataType> void updateMap(BluePrintsBackedFinderService<DataType, ?> service, IndexableGraph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, Map<String, Object> objectsBeingAccessed) {
		// Cast should work like a charm
		Map value = (Map) p.get(toUpdate);
		// As a convention, null values are never stored
		if(value!=null && value.size()>0) {
			// Get previously existing vertices
			String collectionEdgeName = GraphUtils.getEdgeNameFor(p);
			Iterable<Edge> existingIterator = rootVertex.getOutEdges(collectionEdgeName);
			// Do not change previously existing vertices if they correspond to new ones
			// Which is done in that call : as vertex is always looked up before creation, there is little duplication risk
			// or at last that risk should be covered by selected Blueprints implementation
			Collection<Vertex> newVertices = createMapVerticesFor(service, value, cascade, objectsBeingAccessed);
			Map<Vertex, Edge> oldVertices = new HashMap<Vertex, Edge>();
			for(Edge e : existingIterator) {
				Vertex inVertex = e.getInVertex();
				if(newVertices.contains(inVertex)) {
					newVertices.remove(inVertex);
				} else {
					oldVertices.put(inVertex, e);
				}
			}
			// Now the have been collected, remove all old vertices
			for(Map.Entry<Vertex, Edge> entry : oldVertices.entrySet()) {
				database.removeEdge(entry.getValue());
				// TODO also remove map entry vertex assocaited edges
			}
			// And finally add new vertices
			for(Vertex newVertex : newVertices) {
				database.addEdge(rootVertex.getId().toString()+"_to_"+newVertex.getId().toString()+"___"+UUID.randomUUID().toString(), 
								rootVertex, newVertex, collectionEdgeName);
			}
		}
	}

	/**
	 * Update given collection by creating a set of edges/vertices for each element
	 * @param p properties to update associated vertices for
	 * @param toUpdate source object to update
	 * @param rootVertex vertex associated to toUpdate
	 * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
	 * @category update
	 */
	private <DataType> void updateCollection(BluePrintsBackedFinderService<DataType, ?> service, IndexableGraph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, Map<String, Object> objectsBeingAccessed) {
		// Cast should work like a charm
		Collection value = (Collection) p.get(toUpdate);
		// As a convention, null values are never stored
		if(value!=null && value.size()>0) {
			// Get previously existing vertices
			String collectionEdgeName = GraphUtils.getEdgeNameFor(p);
			Iterable<Edge> existingIterator = rootVertex.getOutEdges(collectionEdgeName);
			// Do not change previously existing vertices if they correspond to new ones
			// Which is done in that call : as vertex is always looked up before creation, there is little duplication risk
			// or at elast that risk should be covered by selected Blueprints implementation
			Collection<Vertex> newVertices = createCollectionVerticesFor(service, value, cascade, objectsBeingAccessed);
			Map<Vertex, Edge> oldVertices = new HashMap<Vertex, Edge>();
			for(Edge e : existingIterator) {
				Vertex inVertex = e.getInVertex();
				if(newVertices.contains(inVertex)) {
					newVertices.remove(inVertex);
				} else {
					oldVertices.put(inVertex, e);
				}
			}
			// Now the have been collected, remove all old vertices
			for(Map.Entry<Vertex, Edge> entry : oldVertices.entrySet()) {
				database.removeEdge(entry.getValue());
			}
			// And finally add new vertices
			for(Vertex newVertex : newVertices) {
				database.addEdge(rootVertex.getId().toString()+"_to_"+newVertex.getId().toString()+"___"+UUID.randomUUID().toString(), 
								rootVertex, newVertex, collectionEdgeName);
			}
		}
	}

	/**
	 * Create a collection of vertices for the given collection of values
	 * @param value collection of values to create vertices for
	 * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
	 * @return collection of vertices created by {@link #getVertexFor(Object)}
	 */
	private Collection<Vertex> createCollectionVerticesFor(BluePrintsBackedFinderService<?, ?> service, Collection value, CascadeType cascade, Map<String, Object> objectsBeingAccessed) {
		Collection<Vertex> returned = new HashSet<Vertex>();
		for(Object o : value) {
			returned.add(service.getVertexFor(o, cascade, objectsBeingAccessed));
		}
		return returned;
	}

	/**
	 * Create a collection of map vertices (each representing one map entry) for each entry of the input map.
	 * @param value map of values to create vertices for
	 * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
	 * @return collection of vertices created by {@link #getVertexFor(Object)}
	 * @see #getVertexFor(Object, CascadeType, Map) for details about the way to generate a vertex for a Map.Entry node
	 */
	private Collection<Vertex> createMapVerticesFor(BluePrintsBackedFinderService<?, ?> service, Map value, CascadeType cascade, Map<String, Object> objectsBeingAccessed) {
		Collection<Vertex> returned = new HashSet<Vertex>();
		// Strangely, the entrySet is not seen as a Set<Entry>
		for(Entry o : (Set<Entry>) value.entrySet()) {
			returned.add(service.getVertexFor(o, cascade, objectsBeingAccessed));
		}
		return returned;
	}

	/**
	 * Update single-valued property by changing target of edge used to represent the property
	 * @param p updated property
	 * @param toUpdate updated object
	 * @param rootVertex vertex representing the object
	 * @param cascade used cascade type, can be either {@link CascadeType#PERSIST} or {@link CascadeType#MERGE}
	 * @category update
	 */
	private <DataType> void updateSingle(BluePrintsBackedFinderService<DataType, ?> service, IndexableGraph database, Property p, Object toUpdate, Vertex rootVertex, CascadeType cascade, Map<String, Object> objectsBeingAccessed) {
		Object value = p.get(toUpdate);
		// As a convention, null values are never stored
		if(value!=null) {
			Vertex valueVertex = service.getVertexFor(value, cascade, objectsBeingAccessed);
			Edge link = null;
			// Get previously existing vertex
			String edgeNameFor = GraphUtils.getEdgeNameFor(p);
			Iterator<Edge> existingIterator = rootVertex.getOutEdges(edgeNameFor).iterator();
			// property is single-valued, so iteration can be done at most one
			if(existingIterator.hasNext()) {
				// There is an existing edge, change its target and maybe delete previous one
				Edge existing = existingIterator.next();
				if(existing.getInVertex().equals(valueVertex)) {
					// Nothing to do
					link = existing;
				} else {
					// delete edge (TODO maybe delete vertex)
					database.removeEdge(existing);
					link = database.addEdge(rootVertex.getId().toString()+"_to_"+valueVertex.getId().toString(), 
									rootVertex, valueVertex, edgeNameFor);
				}
			}
			if(existingIterator.hasNext()) {
				if (logger.isLoggable(Level.SEVERE)) {
					// There is some incoherent data in graph .. log it !
					StringBuilder sOut = new StringBuilder("An object with the following monovalued property\n").append(p.toGenericString()).append(" is linked to more than one vertex :");
					while(existingIterator.hasNext()) {
						sOut.append("\n\t").append(existingIterator.next().getInVertex().toString());
					}
					logger.log(Level.SEVERE, "Graph contains some incoherence :"+sOut.toString());
				}
			} else {
				if(link==null)
					// Now create edge
					link = database.addEdge(rootVertex.getId().toString()+"_to_"+valueVertex.getId().toString()+"_for_"+edgeNameFor, 
									rootVertex, valueVertex, edgeNameFor);
			}
		}
	}

	public <DataType> DataType loadObject(BluePrintsBackedFinderService<DataType, ?> service, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		String objectVertexId = objectVertex.getProperty(Properties.vertexId.name()).toString();
		return loadObject(service, objectVertexId, objectVertex, objectsBeingAccessed);
	}

	/**
	 * Load object with given vertex id and vertex node
	 * @param objectVertexId
	 * @param objectVertex
	 * @param objectsBeingAccessed map of objects currently being accessed, it avoid some loops during loading, but is absolutely NOT a persistent cache
	 * @return loaded object
	 */
	public <DataType> DataType loadObject(BluePrintsBackedFinderService<DataType, ?> service, String objectVertexId, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		if(objectsBeingAccessed.containsKey(objectVertexId))
			return (DataType) objectsBeingAccessed.get(objectVertexId);
		// Shortcut
		if(objectVertex==null) {
			objectsBeingAccessed.put(objectVertexId, null);
			return null;
		} else {
			ClassLoader classLoader = service.getContainedClass().getClassLoader();
			ServiceRepository repository = service.getRepository();
			DataType returned = (DataType) GraphUtils.createInstance(classLoader, objectVertex, repository, objectsBeingAccessed);
			Map<Property, Collection<CascadeType>> containedProperties = service.getContainedProperties(returned);
			try {
				objectsBeingAccessed.put(objectVertexId, returned);
				loadObjectProperties(classLoader, repository, objectVertex, returned, containedProperties, objectsBeingAccessed);
				return returned;
			} finally {
//				objectsBeingAccessed.remove(objectVertexId);
			}
		}
	}


	public <DataType> void loadObjectProperties(ClassLoader classLoader, ServiceRepository repository, Vertex objectVertex, DataType returned,
					Map<Property, Collection<CascadeType>> containedProperties, Map<String, Object> objectsBeingAccessed) {
		for(Property p : containedProperties.keySet()) {
			if(!p.hasModifier(Modifier.STATIC) && !Annotations.TRANSIENT.is(p)) {
				Class<?> rawPropertyType = p.getType();
				if(Collection.class.isAssignableFrom(rawPropertyType)) {
					loadCollection(classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
					// each value should be written as an independant value
				} else if(Map.class.isAssignableFrom(rawPropertyType)) {
					loadMap(classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
				} else {
					loadSingle(classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
				}
			}
		}
	}
	/**
	 * Implementation tied to the future implementation of {@link #updateMap(Property, Object, Vertex, CascadeType)}
	 * @param p
	 * @param returned
	 * @param objectVertex
	 */
	private <DataType> void loadMap(ClassLoader classLoader, ServiceRepository repository, Property p, DataType returned, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		boolean eagerLoad = false;
		// property may be associated to a onetomany or manytomany mapping. in such a case, check if there is an eager loading info
		OneToMany oneToMany = p.getAnnotation(OneToMany.class);
		if(oneToMany!=null) {
			eagerLoad = FetchType.EAGER.equals(oneToMany.fetch());
		}
		if(!eagerLoad) {
			ManyToMany manyToMany = p.getAnnotation(ManyToMany.class);
			if(manyToMany!=null) {
				eagerLoad = FetchType.EAGER.equals(manyToMany.fetch());
			}
		}
		Map<Object, Object> generatedCollection = (Map<Object, Object>) Utils.generateMap((Class<?>) p.getType(), null);
		MapLazyLoader handler = new MapLazyLoader(classLoader, repository, p, objectVertex, generatedCollection, objectsBeingAccessed);
		if(eagerLoad) {
			handler.loadMap(generatedCollection, objectsBeingAccessed);
			p.set(returned, generatedCollection);
		} else {
			// Java proxy code
			p.set(returned, Proxy.newProxyInstance(
							classLoader,
							new Class[] { p.getType(), Serializable.class, WriteReplaceable.class },
							handler));
		}
	}

	/**
	 * Load a single-valued property from graph
	 * @param p
	 * @param returned
	 * @param objectVertex
	 * @param objectsBeingAccessed
	 */
	private <DataType> void loadSingle(ClassLoader classloader, ServiceRepository repository, Property p, DataType returned, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		Iterator<Edge> iterator = objectVertex.getOutEdges(GraphUtils.getEdgeNameFor(p)).iterator();
		if(iterator.hasNext()) {
			// yeah, there is a value !
			Vertex firstVertex = iterator.next().getInVertex();
			Object value = GraphUtils.createInstance(classloader, firstVertex, repository, objectsBeingAccessed);
			if(repository.containsKey(value.getClass())) {
				// value requires fields loading
				BluePrintsBackedFinderService blueprints= (BluePrintsBackedFinderService) repository.get(value.getClass());
				p.set(returned, loadObject(blueprints, firstVertex, objectsBeingAccessed));
			} else {
				p.set(returned, value);
			}
		}
		// TODO test unsupported multi-values
	}


	/**
	 * Load collection corresponding to the given property for the given vertex.
	 * BEWARE : here be lazy loading !
	 * @param p
	 * @param returned
	 * @param objectVertex
	 */
	private <DataType> void loadCollection(ClassLoader classLoader, ServiceRepository repository, Property p, DataType returned, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		boolean eagerLoad = false;
		// property may be associated to a onetomany or manytomany mapping. in such a case, check if there is an eager loading info
		OneToMany oneToMany = p.getAnnotation(OneToMany.class);
		if(oneToMany!=null) {
			eagerLoad = FetchType.EAGER.equals(oneToMany.fetch());
		}
		if(!eagerLoad) {
			ManyToMany manyToMany = p.getAnnotation(ManyToMany.class);
			if(manyToMany!=null) {
				eagerLoad = FetchType.EAGER.equals(manyToMany.fetch());
			}
		}
		Collection<Object> generatedCollection = Utils.generateCollection((Class<?>) p.getType(), null);
		CollectionLazyLoader handler = new CollectionLazyLoader(classLoader, repository, p, objectVertex, generatedCollection, objectsBeingAccessed);
		if(eagerLoad) {
			handler.loadCollection(generatedCollection, objectsBeingAccessed);
			p.set(returned, generatedCollection);
		} else {
			// Java proxy code
			p.set(returned, Proxy.newProxyInstance(
							classLoader,
							new Class[] { p.getType(), Serializable.class, WriteReplaceable.class },
							handler));
		}
	}
}
