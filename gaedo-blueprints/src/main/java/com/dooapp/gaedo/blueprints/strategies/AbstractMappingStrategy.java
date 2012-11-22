package com.dooapp.gaedo.blueprints.strategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.GeneratedValue;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.UnsupportedIdException;
import com.dooapp.gaedo.blueprints.UnsupportedIdTypeException;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.extensions.id.IdGenerator;
import com.dooapp.gaedo.extensions.id.IntegerGenerator;
import com.dooapp.gaedo.extensions.id.LongGenerator;
import com.dooapp.gaedo.extensions.id.StringGenerator;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public abstract class AbstractMappingStrategy<DataType> implements GraphMappingStrategy<DataType>{

	protected final PropertyProvider propertyProvider;
	protected final Class<DataType> serviceContainedClass;
	protected final Migrator migrator;
	/**
	 * Property used to store id
	 */
	protected Property idProperty;
	protected boolean idGenerationRequired;
	protected AbstractBluePrintsBackedFinderService<?, DataType, ?> service;

	public AbstractMappingStrategy(Class<DataType> serviceContainedClass, PropertyProvider propertyProvider, Migrator migrator) {
		super();
		this.serviceContainedClass = serviceContainedClass;
		this.propertyProvider = propertyProvider;
		this.migrator = migrator;
		this.idProperty = AnnotationUtils.locateIdField(propertyProvider, serviceContainedClass, Long.TYPE, Long.class, String.class);
		this.idGenerationRequired = idProperty.getAnnotation(GeneratedValue.class) != null;
	}

	/**
	 * This implementation only supports single valued id
	 * @param value
	 * @param id
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#assignId(java.lang.Object, java.lang.Object[])
	 */
	@Override
	public void assignId(DataType value, Object... id) {
		idProperty.set(value, id[0]);
	}

	@Override
	public void generateValidIdFor(DataType toCreate) {
		// Check value of idProperty
		Object value = idProperty.get(toCreate);
		if (value == null) {
			generateIdFor(service, toCreate);
		} else if (Number.class.isAssignableFrom(Utils.maybeObjectify(idProperty.getType()))) {
			Number n = (Number) value;
			if (n.equals(0) || n.equals(0l)) {
				generateIdFor(service, toCreate);
			}
		}
	}

	@Override
	public String getIdString(DataType object) {
		Object objectId = idProperty.get(object);
		// modified due to https://github.com/Riduidel/gaedo/issues/23
		return GraphUtils.getIdOfLiteral(object.getClass(), idProperty, objectId);
	}

	@Override
	public String getAsId(Object object) {
		if (Utils.maybeObjectify(idProperty.getType()).isAssignableFrom(Utils.maybeObjectify(object.getClass()))) {
			return GraphUtils.getIdOfLiteral(serviceContainedClass, idProperty, object);
		} else {
			throw new UnsupportedIdException(object.getClass(), idProperty.getType());
		}
	}

	@Override
	public Collection<Property> getIdProperties() {
		return Arrays.asList(idProperty);
	}

	@Override
	public boolean isIdGenerationRequired() {
		return idGenerationRequired;
	}

	public void generateIdFor(AbstractBluePrintsBackedFinderService<?, DataType, ?> service, DataType toCreate) {
		IdGenerator generator = null;
		Class<?> objectType = Utils.maybeObjectify(idProperty.getType());
		if (Long.class.isAssignableFrom(objectType)) {
			generator = new LongGenerator(service, idProperty);
		} else if (Integer.class.isAssignableFrom(objectType)) {
			generator = new IntegerGenerator(service, idProperty);
		} else if (String.class.isAssignableFrom(objectType)) {
			generator = new StringGenerator(service, idProperty);
		} else {
			throw new UnsupportedIdTypeException(objectType + " can't be used as id : we don't know how to generate its values !");
		}
		generator.generateIdFor(toCreate);
	}

	@Override
	public void reloadWith(AbstractBluePrintsBackedFinderService<?, DataType, ?> service) {
		this.service = service;
	}

	/**
	 * Default implementation will call subclass {@link #addDefaultSearchToAndTest(AndVertexTest)} on an {@link AndVertexTest} that may be created on the fly.
	 * 
	 * @param vertexTest vertex test to add default search to
	 * @return a vertex test with default search installed
	 */
	@Override
	public CompoundVertexTest addDefaultSearchTo(CompoundVertexTest vertexTest) {
		if (vertexTest instanceof AndVertexTest) {
			return addDefaultSearchToAndTest((AndVertexTest) vertexTest);
		} else {
			// Create a new AndTest, add to it current vertex test and test on
			// class
			AndVertexTest used = new AndVertexTest(vertexTest.getDriver(), vertexTest.getPath());
			used.add(vertexTest);
			return addDefaultSearchTo(used);
		}
	}

	/**
	 * Add default search to an And vertex test, which is an obvious prerequisite.
	 * @param vertexTest
	 * @return
	 */
	protected abstract CompoundVertexTest addDefaultSearchToAndTest(AndVertexTest vertexTest);

	@Override
	public Iterable<Edge> getOutEdgesFor(Vertex rootVertex, Property p) {
		String edgeNameFor = GraphUtils.getEdgeNameFor(p);
		Iterable<Edge> allEdges = rootVertex.getOutEdges(edgeNameFor);
		Collection<Edge> returned = new LinkedList<Edge>();
		for(Edge e : allEdges) {
			if(GraphUtils.isInNamedGraphs(e, service.getLens())) {
				returned.add(e);
			}
		}
		return returned;
	}
}
