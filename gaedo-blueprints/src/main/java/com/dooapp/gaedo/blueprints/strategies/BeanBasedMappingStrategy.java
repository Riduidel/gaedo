package com.dooapp.gaedo.blueprints.strategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitor;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class BeanBasedMappingStrategy<DataType> extends AbstractMappingStrategy<DataType> implements GraphMappingStrategy<DataType> {

	/**
	 * Visitor making sure there is a test done on class collection property
	 * @author ndx
	 *
	 */
	private static class TestContainsClassProperty extends VertexTestVisitorAdapter implements VertexTestVisitor {

		private boolean classTested;
		private ClassCollectionProperty classCollectionProperty;

		public TestContainsClassProperty(ClassCollectionProperty classCollectionProperty) {
			this.classCollectionProperty = classCollectionProperty;
		}

		public boolean isClassTested() {
			return classTested;
		}
		
		@Override
		public void visit(CollectionContains collectionContains) {
			List<Property> propertyPath = CollectionUtils.asList(collectionContains.getPath());
			classTested = classTested || propertyPath.size()==1 && propertyPath.contains(classCollectionProperty);
		}
	}

	static final Logger logger = Logger.getLogger(BeanBasedMappingStrategy.class.getName());

	/**
	 * Cach linking classes to their property maps
	 */
	private Map<Class<?>, Map<Property, Collection<CascadeType>>> classes = new HashMap<Class<?>, Map<Property,Collection<CascadeType>>>();

	public BeanBasedMappingStrategy(Class<DataType> serviceContainedClass, PropertyProvider propertyProvider, Migrator migrator) {
		super(serviceContainedClass, propertyProvider, migrator);
	}
	
	@Override
	public Map<Property, Collection<CascadeType>> getContainedProperties(DataType object, Vertex vertex, CascadeType cascadeType) {
		Class<? extends Object> objectClass = object.getClass();
		return getContainedProperties(objectClass);
	}

	public Map<Property, Collection<CascadeType>> getContainedProperties(Class<? extends Object> objectClass) {
		if (!classes.containsKey(objectClass)) {
			Map<Property, Collection<CascadeType>> beanPropertiesFor = StrategyUtils.getBeanPropertiesFor(propertyProvider, objectClass, migrator);
			// Finally, create a fake "classesCollection" property and add it to
			// property
			try {
				beanPropertiesFor.put(new ClassCollectionProperty(objectClass), new LinkedList<CascadeType>());
				beanPropertiesFor.put(new TypeProperty(objectClass), new LinkedList<CascadeType>());
			} catch (Exception e) {
				logger.log(Level.SEVERE, "what ? a class without a \"class\" field ? WTF", e);
			}
			classes .put(objectClass, beanPropertiesFor);
		}
		return classes.get(objectClass);
	}

	/**
	 * Here, default test consists into checking the class collection contains {@link AbstractMappingStrategy#serviceContainedClass}
	 * @param vertexTest
	 * @return
	 * @see com.dooapp.gaedo.blueprints.strategies.AbstractMappingStrategy#addDefaultSearchToAndTest(com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest)
	 */
	@Override
	protected CompoundVertexTest addDefaultSearchToAndTest(AndVertexTest vertexTest) {
		ClassCollectionProperty classCollectionProperty = new ClassCollectionProperty(serviceContainedClass);
		TestContainsClassProperty visitor = new TestContainsClassProperty(classCollectionProperty);
		vertexTest.accept(visitor);
		if(!visitor.isClassTested()) {
			CollectionContains objectClassContains = new CollectionContains(vertexTest.getDriver(),
							Arrays.asList(new Property[] { classCollectionProperty }), serviceContainedClass);
			vertexTest.add(objectClassContains);
		}
		return vertexTest;
	}

	@Override
	public String getEffectiveType(Vertex vertex) {
		if(vertex.getProperty(Properties.type.name())!=null) {
			return TypeUtils.getClass(vertex.getProperty(Properties.type.name()).toString());
		} else {
            if(Kind.literal.equals(GraphUtils.getKindOf(vertex))) {
                // a literal with no type information should always been considered a string literal
                return STRING_CLASS;
            } else {
                Edge toType = vertex.getOutEdges(IndexableGraphBackedFinderService.TYPE_EDGE_NAME).iterator().next();
                Vertex type = toType.getInVertex();
                // Do not use ClassLiteral here as this method must be blazing fast
                return IndexableGraphBackedFinderService.classTransformer.extractClassIn(service.getDriver().getValue(type).toString());
            }
		}
	}

	@Override
	public void loaded(String fromId, Vertex from, DataType into, Map<String, Object> objectsBeingAccessed) {
	}

	/**
	 * In bean based strategy, all beans should be loaded, whatever their depth is
	 * @param objectVertexId
	 * @param objectVertex
	 * @param objectsBeingAccessed
	 * @return
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#shouldLoadPropertiesOf(java.lang.String, com.tinkerpop.blueprints.pgm.Vertex, java.util.Map)
	 */
	@Override
	public boolean shouldLoadPropertiesOf(String objectVertexId, Vertex objectVertex, Map<String, Object> objectsBeingAccessed) {
		return true;
	}
}
