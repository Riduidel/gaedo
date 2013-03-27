package com.dooapp.gaedo.blueprints.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.dynabeans.BagProperty;
import com.dooapp.gaedo.blueprints.dynabeans.PropertyMapPropertyAccess;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexPropertyTest;
import com.dooapp.gaedo.blueprints.strategies.graph.GraphBasedPropertyBuilder;
import com.dooapp.gaedo.blueprints.strategies.graph.GraphFieldLocator;
import com.dooapp.gaedo.blueprints.strategies.graph.NoEdgeInNamedGraphsException;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.DelegatingInformerLocator;
import com.dooapp.gaedo.finders.root.FieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * When using this strategy, properties will be directly determined from edges
 * exiting from edited node.
 * 
 * @author ndx
 * 
 * @param <DataType>
 */
public class GraphBasedMappingStrategy<DataType> extends AbstractMappingStrategy<DataType> implements GraphMappingStrategy<DataType> {
	private static final Logger logger = Logger.getLogger(GraphBasedMappingStrategy.class.getName());

    /**
	 * Default mapped properties
	 */
	private Map<Property, Collection<CascadeType>> beanPropertiesForServiceClass;
	
	/**
	 * Obejct storing the currently loaded id. Most of this time this value will be null. But when a loading operation starts
	 * a value will be stored there.
	 */
	private ThreadLocal<String> loadedByThisThread = new ThreadLocal<String>();

	public GraphBasedMappingStrategy(Class<DataType> serviceContainedClass, PropertyProvider propertyProvider, Migrator migrator) {
		super(serviceContainedClass, propertyProvider, migrator);
		beanPropertiesForServiceClass = StrategyUtils.getBeanPropertiesFor(propertyProvider, serviceContainedClass, migrator);
		// an auxiliary list is created to navigate properties, as we will
		// remove some of them from beanProperties,
		// and that is not something iterators are good at
		List<Property> propertyNavigator = new LinkedList<Property>(beanPropertiesForServiceClass.keySet());
		// Replace id property by one allowing zero navigation (for findById and
		// other methods)
		for (Property p : propertyNavigator) {
			if (p.getAnnotation(BagProperty.class) != null) {
				beanPropertiesForServiceClass.remove(p);
			}
		}

	}

	/**
	 * To get contained property, we simply build a map of edges outgoing from
	 * vertex and associate to them cascade information based on ... what ?
	 * magic ?
	 * 
	 * @param object
	 *            object to load data in
	 * @param vertex
	 *            associated vertew which will never be left null
	 * @return mapping between properties representing edges and cascade for
	 *         these properties
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#getContainedProperties(java.lang.Object,
	 *      com.tinkerpop.blueprints.pgm.Vertex, CascadeType)
	 */
	@Override
	public Map<Property, Collection<CascadeType>> getContainedProperties(DataType object, Vertex vertex, CascadeType cascadeType) {
		// notice that, as default, properties only cascade creation
		Map<Property, Collection<CascadeType>> returned = new HashMap<Property, Collection<CascadeType>>();
		// During persist operation, we only persist graph properties, and NOT
		// object ones (that would polute the graph)
		if (!CascadeType.MERGE.equals(cascadeType) && !CascadeType.PERSIST.equals(cascadeType))
			returned.putAll(beanPropertiesForServiceClass);
		// extract edges from persistable object infos
		if (object instanceof PropertyMapPropertyAccess) {
			PropertyMapPropertyAccess propertyBag = (PropertyMapPropertyAccess) object;
			for (Property uri : propertyBag.propertyUris()) {
				returned.put(uri, StrategyUtils.extractCascadeOfJPAAnnotations(uri));
			}
		}
		if (vertex == null) {
		} else {
			Map<String, GraphBasedPropertyBuilder<DataType>> edgeLabelToProperty = new TreeMap<String, GraphBasedPropertyBuilder<DataType>>();
			for (Edge e : vertex.getEdges(Direction.OUT)) {
				String edgeLabel = e.getLabel();
				if (!edgeLabelToProperty.containsKey(edgeLabel)) {
					edgeLabelToProperty.put(edgeLabel, new GraphBasedPropertyBuilder<DataType>(serviceContainedClass, service.getDriver(), service.getLens()));
				}
				edgeLabelToProperty.get(edgeLabel).add(e);
			}
			for (GraphBasedPropertyBuilder<DataType> builder : edgeLabelToProperty.values()) {
				try {
					Property built = builder.build();
					// only add property if absent from properties laoded from the bean, as properties loaded from the bean are respectfull to initial data type
					if(!returned.containsKey(built))
						returned.put(built, StrategyUtils.extractCascadeOfJPAAnnotations(built));
				} catch(NoEdgeInNamedGraphsException e) {
					logger.info(e.getMessage());
				}
			}
		}
		return returned;
	}

	/**
	 * When loading that strategy, we also customize the informer factory to
	 * make sure non-existing properties
	 * 
	 * @param service
	 * @see com.dooapp.gaedo.blueprints.strategies.AbstractMappingStrategy#reloadWith(com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService)
	 */
	@Override
	public void reloadWith(AbstractBluePrintsBackedFinderService<?, DataType, ?> service) {
		super.reloadWith(service);
		InformerFactory informerFactory = service.getInformerFactory();
		if (informerFactory instanceof ProxyBackedInformerFactory) {
			ProxyBackedInformerFactory initialProxy = (ProxyBackedInformerFactory) informerFactory;
			ReflectionBackedInformerFactory initialReflective = initialProxy.getReflectiveInformerFactory();
			
			FieldInformerLocator initialLocator = initialReflective.getFieldLocator();
			// nothing has been loaded yet
			if(initialLocator instanceof CumulativeFieldInformerLocator) {
				ReflectionBackedInformerFactory usedReflective = new ReflectionBackedInformerFactory(
								new DelegatingInformerLocator(initialLocator, createLocator(initialLocator)),
								initialReflective.getPropertyProvider()
								);
				
				ProxyBackedInformerFactory usedProxy = new ProxyBackedInformerFactory(usedReflective);
				service.setInformerFactory(usedProxy);
			} else if(initialLocator instanceof DelegatingInformerLocator) {
				DelegatingInformerLocator delegating = (DelegatingInformerLocator) initialLocator;
				delegating.setSecond(createLocator(delegating.getFirst()));
			}
		}
	}

	private GraphFieldLocator<DataType> createLocator(FieldInformerLocator cumulativeLocator) {
		return new GraphFieldLocator<DataType>(
						serviceContainedClass, 
						service.getInformer().getClass(), 
						service.getDatabase(), service
						.getDriver(),
		/*
		 * yup, there is a loop here, but for a good reason :
		 * graphFieldLocator will generate a Property out of an edge, then
		 * cumulative locator will create a FieldInformer out of that
		 * property
		 */
		cumulativeLocator,
		service.getLens());
	}

	@Override
	protected CompoundVertexTest addDefaultSearchToAndTest(AndVertexTest vertexTest) {
		vertexTest.add(new VertexPropertyTest(this, service.getDriver(), new LinkedList<Property>(), Properties.kind.name(), Kind.uri.name()));
		return vertexTest;
	}

	/**
	 * Any vertex uses as default the service contained class type
	 * 
	 * @param vertex
	 * @return
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#getEffectiveType(com.tinkerpop.blueprints.pgm.Vertex)
	 */
	@Override
	public String getEffectiveType(Vertex vertex) {
		Kind kind = Kind.valueOf(vertex.getProperty(Properties.kind.name()).toString());
		switch (kind) {
		case literal:
			Object typeProperty = vertex.getProperty(Properties.type.name());
			if (typeProperty == null)
				throw new UnableToGetVertexTypeException();
			return TypeUtils.getClass(typeProperty.toString());
		case uri:
			return serviceContainedClass.getName();
		case bnode:
		default:
			throw new UnableToGetVertexTypeException();
		}
	}

	@Override
	public String getAsId(Object object) {
		return Literals.get(object.getClass()).getVertexId(object);
	}
	
	@Override
	public void loaded(String fromId, Vertex from, DataType into, Map<String, Object> objectsBeingAccessed) {
		// when this happens, it means we finally loaded the initial object
		if(fromId.equals(loadedByThisThread.get()))
			loadedByThisThread.remove();
		idProperty.set(into, from.getProperty(Properties.value.name()));
	}

	/**
	 * What are the objects requiring loading ? The ones that are directly accessed.
	 * As a consequence, we use a {@link ThreadLocal} mechanism to store objects ids being loaded.
	 * In other words, if thread local contains no object id, put current one.
	 * If there is one (which is not current one, return false).
	 * Notice the {@link #loaded(String, Vertex, Object, Map)} method, when invoked, will clear that info for queries to run smoothly
	 * @param objectVertexId
	 * @param objectVertex
	 * @param objectsBeingAccessed
	 * @return
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#shouldLoadPropertiesOf(java.lang.String, com.tinkerpop.blueprints.pgm.Vertex, java.util.Map)
	 */
	@Override
	public boolean shouldLoadPropertiesOf(String objectVertexId, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		if(loadedByThisThread.get()==null) {
			loadedByThisThread.set(objectVertexId);
		}
		return loadedByThisThread.get().equals(objectVertexId);
	}

	@Override
	public GraphMappingStrategy<DataType> derive() {
		return new GraphBasedMappingStrategy<DataType>(serviceContainedClass, propertyProvider, migrator);
	}
}
