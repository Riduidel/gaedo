package com.dooapp.gaedo.blueprints.operations;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.CantCreateAVertexForALiteralException;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class Deleter {
	public class DeleteProperties<DataType> extends AbstractCardinalityDistinguishingOperation {
		private final Map<Property, Collection<CascadeType>> containedProperties;
		private final Graph database;
		private final ObjectCache objectsBeingAccessed;
		private final Vertex objectVertex;
		private final DataType toDelete;
		private final AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service;

		private DeleteProperties(Map<Property, Collection<CascadeType>> containedProperties, Graph database, ObjectCache objectsBeingAccessed,
						Vertex objectVertex, DataType toDelete, AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service) {
			this.containedProperties = containedProperties;
			this.database = database;
			this.objectsBeingAccessed = objectsBeingAccessed;
			this.objectVertex = objectVertex;
			this.toDelete = toDelete;
			this.service = service;
		}

		@Override
		protected void operateOnSingle(Property p, CascadeType cascade) {
		    Collection<CascadeType> toCascade = containedProperties.get(p);
		    deleteSingle(service, database, p, toDelete, objectVertex, toCascade, objectsBeingAccessed);
		}

		@Override
		protected void operateOnMap(Property p, CascadeType cascade) {
		    Collection<CascadeType> toCascade = containedProperties.get(p);
		    deleteMap(service, database, p, toDelete, objectVertex, toCascade, objectsBeingAccessed);
		}

		@Override
		protected void operateOnCollection(Property p, CascadeType cascade) {
		    Collection<CascadeType> toCascade = containedProperties.get(p);
		    deleteCollection(service, database, p, toDelete, objectVertex, toCascade, objectsBeingAccessed);
		}
	}

	private static final Logger logger = Logger.getLogger(Deleter.class.getName());
    /**
     * Delete given object
     *
     * @param service              source of modification
     * @param driver TODO
     * @param objectVertexId       object expected vertex id
     * @param objectVertex         vertex corresponding to object to delete
     * @param valueClass           class contained by service
     * @param containedProperties  list of contained properties
     * @param toDelete             object to delete
     * @param cascade              kind of cascade used for dependent properties
     * @param objectsBeingAccessed map containing subgraph of objects currently being delete, this is used to avoid loops, and NOT as a cache
     */
    public <DataType> void performDelete(
    				final AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service,
    				final GraphDatabaseDriver driver,
    				final String objectVertexId,
    				final Vertex objectVertex,
    				final Class<?> valueClass,
    				final Map<Property, Collection<CascadeType>> containedProperties,
    				final DataType toDelete,
    				CascadeType cascade,
    				final ObjectCache objectsBeingAccessed) {
        final Graph database = service.getDatabase();
        new OperateOnProperties().execute(containedProperties, cascade, new DeleteProperties<DataType>(containedProperties, database, objectsBeingAccessed, objectVertex, toDelete, service));
        /* We try to locate vertex in graph before to delete it. Indeed, mainly due cascade delete, this vertex may have already been removed */
        Vertex notYetDeleted = driver.loadVertexFor(objectVertexId, valueClass.getName());
        if(notYetDeleted!=null)
        	driver.removeSafely(notYetDeleted);
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
        // there should be only one vertex to delete, excepted in the case of literal values, which are stored in vertex itself
        Iterable<Edge> edges = service.getStrategy().getOutEdgesFor(objectVertex, p);
        for (Edge e : edges) {
            Vertex valueVertex = e.getVertex(Direction.IN);
            GraphUtils.removeSafely(database, e);
            // Now what to do with vertex ? Delete it ?
            if (toCascade.contains(CascadeType.REMOVE)) {
                // yes, delete it forever (but before, see if there aren't more datas to delete
                Object value = p.get(toDelete);
                if(value!=null) {
                	try {
	                	Vertex knownValueVertex = service.getVertexFor(value, CascadeType.REFRESH, objectsBeingAccessed);
	                	if(knownValueVertex!=null && knownValueVertex.equals(valueVertex))
	                		service.deleteOutEdgeVertex(objectVertex, valueVertex, value, objectsBeingAccessed);
                	} catch(CantCreateAVertexForALiteralException vertexWontBeDeleted) {
                		// According to https://github.com/Riduidel/gaedo/issues/85, this case should be simply ignored ...
                		// but I'll nevertheless write a memo log
                		if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING,
											String.format("You tried to delete a vertex for a literal which is what is said here :\n\t%s"
															+ "\nThis is not possible. As a consequence, vertex %s\nwill continue "
															+ "it's polluting lifestyle in your graph.",
															vertexWontBeDeleted.getMessage(),
															GraphUtils.toString(valueVertex)));
						}
                	}
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

}
