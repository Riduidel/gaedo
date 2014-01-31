package com.dooapp.gaedo.blueprints.operations;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.CollectionLazyLoader;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.MapLazyLoader;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.ObjectCache.ValueLoader;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralHelper;
import com.dooapp.gaedo.blueprints.transformers.ClassIsNotAKnownLiteralException;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

public class Loader {
    public final class LoadProperties<DataType> extends AbstractCardinalityDistinguishingOperation {
		private final ObjectCache objectsBeingAccessed;
		private final ClassLoader classLoader;
		private final DataType returned;
		private final Vertex objectVertex;
		private final GraphMappingStrategy strategy;
		private final ServiceRepository repository;
		private final GraphDatabaseDriver driver;

		private LoadProperties(ObjectCache objectsBeingAccessed, ClassLoader classLoader, DataType returned, Vertex objectVertex,
						GraphMappingStrategy strategy, ServiceRepository repository, GraphDatabaseDriver driver) {
			this.objectsBeingAccessed = objectsBeingAccessed;
			this.classLoader = classLoader;
			this.returned = returned;
			this.objectVertex = objectVertex;
			this.strategy = strategy;
			this.repository = repository;
			this.driver = driver;
		}

		@Override
		protected void operateOnSingle(Property p, CascadeType cascade) {
		    loadSingle(driver, strategy, classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
		}

		@Override
		protected void operateOnMap(Property p, CascadeType cascade) {
		    loadMap(driver, strategy, classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
		}

		@Override
		protected void operateOnCollection(Property p, CascadeType cascade) {
		    loadCollection(driver, strategy, classLoader, repository, p, returned, objectVertex, objectsBeingAccessed);
		}
	}


	/**
     * @deprecated due to {@link AbstractBluePrintsBackedFinderService#getDriver()} deprecation
     * @see #loadObject(AbstractBluePrintsBackedFinderService, GraphDatabaseDriver, Vertex, ObjectCache)
     */
    @Deprecated
    public <DataType> DataType loadObject(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
        return loadObject(service, service.getDriver(), objectVertex, objectsBeingAccessed);
    }


    public <DataType> DataType loadObject(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, GraphDatabaseDriver driver, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
        String objectVertexId = driver.getIdOf(objectVertex);
        return loadObject(service, driver, objectVertexId, objectVertex, objectsBeingAccessed);
    }

    /**
     * Load object with given vertex id and vertex node
     * @param driver TODO
     * @param objectVertexId
     * @param objectVertex
     * @param objectsBeingAccessed map of objects currently being accessed, it avoid some loops during loading, but is absolutely NOT a persistent cache
     *
     * @return loaded object
     */
    public <DataType> DataType loadObject(final AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, final GraphDatabaseDriver driver, final String objectVertexId, final Vertex objectVertex, final ObjectCache objectsBeingAccessed) {
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
		            DataType returned = (DataType) GraphUtils.createInstance(driver, service.getStrategy(), classLoader, objectVertex, Object.class /* we use object here, as this default type should not be used */, repository, objectsBeingAccessed);
		            try {
		                if (service.getStrategy().shouldLoadPropertiesOf(objectVertexId, objectVertex, objectsBeingAccessed)) {
		                    Map<Property, Collection<CascadeType>> containedProperties = service.getStrategy().getContainedProperties(returned, objectVertex, CascadeType.MERGE);
		                    objectsBeingAccessed.put(objectVertexId, returned);
		                    loadObjectProperties(driver, service.getStrategy(), classLoader, repository, objectVertex, returned, containedProperties, objectsBeingAccessed);
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


    public <DataType> void loadObjectProperties(
    				final GraphDatabaseDriver driver,
    				final GraphMappingStrategy strategy,
    				final ClassLoader classLoader,
    				final ServiceRepository repository,
    				final Vertex objectVertex,
                    final DataType returned,
                    final Map<Property, Collection<CascadeType>> containedProperties,
                    final ObjectCache objectsBeingAccessed) {
    	new OperateOnProperties().execute(containedProperties, CascadeType.REFRESH, new LoadProperties<DataType>(objectsBeingAccessed, classLoader, returned, objectVertex, strategy, repository, driver));
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
        Object value = null;
        if (iterator.hasNext()) {
            // yeah, there is a value !
            Edge edge = iterator.next();
            Vertex firstVertex = edge.getVertex(Direction.IN);
            value = GraphUtils.createInstance(driver, strategy, classloader, firstVertex, p.getType(), repository, objectsBeingAccessed);
            if (repository.containsKey(value.getClass())) {
                // value requires fields loading
                AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, ?> blueprints = (AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, ?>) repository.get(value.getClass());
                value = loadObject(blueprints, firstVertex, objectsBeingAccessed);
            }
        } else {
        	if(hasLiteralProperty(p, objectVertex)) {
	        	value = loadSingleLiteral(classloader, p, objectVertex, objectsBeingAccessed);
        	}
        }
        if(value!=null) {
            p.set(returned, value);
        }
    }


	public boolean hasLiteralProperty(Property p, Vertex objectVertex) {
		return objectVertex.getProperty(GraphUtils.getEdgeNameFor(p))!=null;
	}


	public Object loadSingleLiteral(ClassLoader classloader, Property p, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
		// if no edge exist, we may be in the case of a literal stored in node property. In that case, check if vertex has a property having same name than edge
		String propertyValue = objectVertex.getProperty(GraphUtils.getEdgeNameFor(p));
		if(propertyValue==null)
			return null;
		return LiteralHelper.getLiteralFromText(classloader, objectsBeingAccessed, p, propertyValue);
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
