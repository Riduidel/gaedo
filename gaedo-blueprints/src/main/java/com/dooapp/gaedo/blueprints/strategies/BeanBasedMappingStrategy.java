package com.dooapp.gaedo.blueprints.strategies;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitor;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.blueprints.transformers.ClassIdentifierHelper;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class BeanBasedMappingStrategy<DataType> extends AbstractMappingStrategy<DataType> implements GraphMappingStrategy<DataType> {

	private static final class ManyObjectsToOneType implements ManyToOne {
		public static final CascadeType[] CASCADE = new CascadeType[] {
			CascadeType.PERSIST,
			CascadeType.MERGE
		};
		private final Class target;
		public ManyObjectsToOneType(Class target) {
			super();
			this.target = target;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return ManyToOne.class;
		}

		@Override
		public Class targetEntity() {
			return target;
		}

		@Override
		public boolean optional() {
			return false;
		}

		@Override
		public FetchType fetch() {
			return FetchType.EAGER;
		}

		@Override
		public CascadeType[] cascade() {
			return CASCADE;
		}
	}

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
				ClassCollectionProperty classes = new ClassCollectionProperty(objectClass).withAnnotation(new ManyObjectsToOneType(objectClass.getClass()));
				beanPropertiesFor.put(classes, StrategyUtils.extractCascadeOfJPAAnnotations(classes));
				TypeProperty type = new TypeProperty().withAnnotation(new ManyObjectsToOneType(objectClass.getClass()));
				beanPropertiesFor.put(type, StrategyUtils.extractCascadeOfJPAAnnotations(type));
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
			CollectionContains objectClassContains = new CollectionContains(this,
							vertexTest.getDriver(), Arrays.asList(new Property[] { classCollectionProperty }), serviceContainedClass);
			vertexTest.add(objectClassContains);
		}
		return vertexTest;
	}

	@Override
	public String getEffectiveType(Vertex vertex) {
		if(vertex.getProperty(Properties.type.name())!=null) {
			return TypeUtils.getClass(vertex.getProperty(Properties.type.name()).toString());
		} else if(vertex.getProperty(GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE))!=null) {
			String typePropertyText = vertex.getProperty(GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE));
			return TypeUtils.getClass(typePropertyText);
		} else {
			// This part of code is totally deprecated, but kept for .. well, no good reason
            if(Kind.literal.equals(GraphUtils.getKindOf(vertex))) {
                // a literal with no type information should always been considered a string literal
            } else {
                Iterator<Edge> typeIterator = vertex.getEdges(Direction.OUT, IndexableGraphBackedFinderService.TYPE_EDGE_NAME).iterator();
                if(typeIterator.hasNext()) {
                    Edge toType = typeIterator.next();
                    Vertex type = toType.getVertex(Direction.IN);
                    // Do not use ClassLiteral here as this method must be blazing fast
                    String value = type.getProperty(Properties.value.name());
                    /*
                     *  usage of edge implies we refer to a remote class vertex, in which class is stored as an URI
                     *  (for compliance with the URI kind we set for class vertices). As a consequence, we have to use
                     *  that getValueIn call to obtain the effective class name
                     */
                    return ClassIdentifierHelper.getValueIn(value);
                }
            }
		}
    	throw new UnableToGetVertexTypeException("vertex "+GraphUtils.toString(vertex)+" provides no way to get its type (through either type property or "+IndexableGraphBackedFinderService.TYPE_EDGE_NAME+" edge");
	}

	@Override
	public void loaded(String fromId, Vertex from, DataType into, ObjectCache objectsBeingAccessed) {
	}

	/**
	 * In bean based strategy, all beans should be loaded, whatever their depth is
	 * @param objectVertexId
	 * @param objectVertex
	 * @param objectsBeingAccessed
	 * @return
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#shouldLoadPropertiesOf(java.lang.String, com.tinkerpop.blueprints.pgm.Vertex, ObjectCache)
	 */
	@Override
	public boolean shouldLoadPropertiesOf(String objectVertexId, Vertex objectVertex, ObjectCache objectsBeingAccessed) {
		return true;
	}

	@Override
	public BeanBasedMappingStrategy derive() {
		return new BeanBasedMappingStrategy<DataType>(serviceContainedClass, propertyProvider, migrator);
	}
}
