package com.dooapp.gaedo.blueprints.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.dynabeans.BagProperty;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexPropertyTest;
import com.dooapp.gaedo.blueprints.strategies.graph.GraphBasedPropertyBuilder;
import com.dooapp.gaedo.blueprints.strategies.graph.GraphFieldLocator;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.FieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * When using this strategy, properties will be directly determined from edges exiting from edited node.
 * @author ndx
 *
 * @param <DataType>
 */
public class GraphBasedMappingStrategy<DataType> extends AbstractMappingStrategy<DataType>  implements GraphMappingStrategy<DataType> {
	private static final String STRING_CLASS = String.class.getName();

	/**
	 * Default mapped properties
	 */
	private Map<Property, Collection<CascadeType>> beanPropertiesForServiceClass;

	public GraphBasedMappingStrategy(Class<DataType> serviceContainedClass, PropertyProvider propertyProvider, Migrator migrator) {
		super(serviceContainedClass, propertyProvider, migrator);
		beanPropertiesForServiceClass = StrategyUtils.getBeanPropertiesFor(propertyProvider, serviceContainedClass, migrator);
		// an auxiliary list is created to navigate properties, as we will remove some of them from beanProperties,
		// and that is not something iterators are good at
		List<Property> propertyNavigator = new LinkedList<Property>(beanPropertiesForServiceClass.keySet());
		// Replace id property by one allowing zero navigation (for findById and other methods)
		for(Property p : propertyNavigator) {
			if(p.getAnnotation(BagProperty.class)!=null) {
				beanPropertiesForServiceClass.remove(p);
			}
		}
		
	}
	/**
	 * To get contained property, we simply build a map of edges outgoing from vertex and associate to them cascade information based on ... what ? magic ?
	 * @param object object to load data in
	 * @param vertex associated vertew which will never be left null
	 * @return mapping between properties representing edges and cascade for these properties
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#getContainedProperties(java.lang.Object, com.tinkerpop.blueprints.pgm.Vertex, CascadeType)
	 */
	@Override
	public Map<Property, Collection<CascadeType>> getContainedProperties(DataType object, Vertex vertex, CascadeType cascadeType) {
		// notice that, as default, properties only cascade creation
		Map<Property, Collection<CascadeType>> returned = new HashMap<Property, Collection<CascadeType>>();
		// During persist operation, we only persist graph properties, and NOT object ones (that would polute the graph)
		if(!CascadeType.MERGE.equals(cascadeType) && !CascadeType.PERSIST.equals(cascadeType))
			returned.putAll(beanPropertiesForServiceClass);
		Map<String, GraphBasedPropertyBuilder<DataType>> edgeLabelToProperty = new TreeMap<String, GraphBasedPropertyBuilder<DataType>>();
		for(Edge e : vertex.getOutEdges()) {
			String edgeLabel = e.getLabel();
			if(!edgeLabelToProperty.containsKey(edgeLabel)) {
				edgeLabelToProperty.put(edgeLabel, 
								new GraphBasedPropertyBuilder<DataType>(serviceContainedClass, service.getDriver()));
			}
			edgeLabelToProperty.get(edgeLabel).add(e);
		}
		for(GraphBasedPropertyBuilder<DataType> builder : edgeLabelToProperty.values()) {
			Property built = builder.build();
			returned.put(built, StrategyUtils.extractCascadeOfJPAAnnotations(built));
		}
		return returned;
	}

	/**
	 * When loading that strategy, we also customize the informer factory to make sure non-existing properties  
	 * @param service
	 * @see com.dooapp.gaedo.blueprints.strategies.AbstractMappingStrategy#loadWith(com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService)
	 */
	@Override
	public void loadWith(AbstractBluePrintsBackedFinderService<?, DataType, ?> service) {
		super.loadWith(service);
		InformerFactory informerFactory = service.getInformerFactory();
		if (informerFactory instanceof ProxyBackedInformerFactory) {
			ProxyBackedInformerFactory proxied = (ProxyBackedInformerFactory) informerFactory;
			addGraphLocatorTo(proxied.getReflectiveInformerFactory());
		}
	}
	
	private void addGraphLocatorTo(ReflectionBackedInformerFactory reflectionInformerFactory) {
		/* To support the multiple case, the field lcoator, which is used by the reflection informer factory
		 * is a CumulativeFieldInformerLocator. We will add one field locator allowing graph search.
		 * This field locator will be able to provide a basic field informer for all relationships visible in graph.
		 */
		FieldInformerLocator locator = reflectionInformerFactory.getFieldLocator();
		if (locator instanceof CumulativeFieldInformerLocator) {
			CumulativeFieldInformerLocator cumulativeLocator = (CumulativeFieldInformerLocator) locator;
			cumulativeLocator.add(new GraphFieldLocator<DataType>(serviceContainedClass, service.getInformer().getClass(), service.getDatabase(), service.getDriver(),
							/* yup, there is a look here, but for a good reason : graphFieldLocator will generate a Property out of an edge, then
							 * cumulative locator will create a FieldInformer out of that property
							 */
							cumulativeLocator));
		}
	}
	
	@Override
	protected CompoundVertexTest addDefaultSearchToAndTest(AndVertexTest vertexTest) {
		vertexTest.add(new VertexPropertyTest(service.getDriver(), new LinkedList<Property>(), Properties.kind.name(), Kind.uri.name()));
		return vertexTest;
	}
	
	/**
	 * Any vertex uses as default the service contained class type
	 * @param vertex
	 * @return
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#getEffectiveType(com.tinkerpop.blueprints.pgm.Vertex)
	 */
	@Override
	public String getEffectiveType(Vertex vertex) {
		Kind kind = Kind.valueOf(vertex.getProperty(Properties.kind.name()).toString());
		switch(kind) {
		case literal:
			Object typeProperty = vertex.getProperty(Properties.type.name());
			if(typeProperty==null)
				return STRING_CLASS;
			return TypeUtils.getClass(typeProperty.toString());
		case uri:
			return serviceContainedClass.getName();
		case bnode:
		default:
			return STRING_CLASS;
		}
	}
	
	@Override
	public void loaded(Vertex from, DataType into) {
		idProperty.set(into, from.getProperty(Properties.value.name()));
	}
	
	@Override
	public String getAsId(Object object) {
		return Literals.get(object.getClass()).getVertexId(object);
	}
}
