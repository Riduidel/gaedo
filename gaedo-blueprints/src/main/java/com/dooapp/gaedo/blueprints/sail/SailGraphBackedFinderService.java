package com.dooapp.gaedo.blueprints.sail;

import java.util.UUID;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.transformers.ClassLiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TupleTransformer;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.impls.sail.SailVertex;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class SailGraphBackedFinderService<DataType, InformerType extends Informer<DataType>> extends
				AbstractBluePrintsBackedFinderService<SailGraph, DataType, Informer<DataType>> {

	private static final String GAEDO = "gaedo";

	public static final String TYPE_EDGE_NAME = GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE);

	private ClassLiteralTransformer classTransformer = (ClassLiteralTransformer) Literals.get(Class.class);

	public SailGraphBackedFinderService(Class<DataType> containedClass, Class<Informer<DataType>> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, SailGraph graph) {
		super(graph, containedClass, informerClass, factory, repository, provider);
		// at creation, make sure graph supports the gaedo prefix
		graph.addNamespace(GAEDO, GraphUtils.GAEDO_CONTEXT);
		graph.addDefaultNamespaces();
	}

	@Override
	public Vertex loadVertexFor(String objectVertexId, String className) {
		return database.getVertex(objectVertexId);
	}

	@Override
	public String getIdOfVertex(Vertex objectVertex) {
		return objectVertex.getId().toString();
	}

	/**
	 * When creating an empty vertex, we immediatly link it to its associated
	 * type vertex : a long will as a consequence be linked to the Long class
	 * 
	 * @param vertexId
	 * @param valueClass
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService#createEmptyVertex(java.lang.String,
	 *      java.lang.Class)
	 */
	@Override
	protected Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass) {
		// Vertex will be created after having selected which node kind is to be
		// used, as the id used by SailGraph IS meaningfull :
		// for a literal, it must be a Literal URI
		Vertex returned = null;
		try {
			if (Literals.containsKey(valueClass)) {
				// some literals aren't so ... literal, as they can accept
				// incoming connections (like classes)
				LiteralTransformer literalTransformer = Literals.get(valueClass);
				Kind kind = literalTransformer.getKind();
				returned = database.addVertex(kind.getURIFor(vertexId, valueClass));
				if (Kind.literal.equals(kind)) {
					returned.setProperty(Properties.type.name(), TypeUtils.getType(valueClass));
				}
			} else {
				if (repository.containsKey(valueClass)) {
					returned = database.addVertex(Kind.uri.getURIFor(vertexId, valueClass));
				} else if (Tuples.containsKey(valueClass)) {
					TupleTransformer tupleTransformer = Tuples.get(valueClass);
					returned = database.addVertex(tupleTransformer.getKind().getURIFor(vertexId, valueClass));
				}
				// obtain vertex for type
				Vertex classVertex = classTransformer.getVertexFor(getDriver(), valueClass);
				Edge toType = getDriver().addEdgeFor(returned, classVertex, TypeProperty.INSTANCE);
				/*
				 * Make sure literals are literals by changing that particular
				 * edge context to a null value. Notice we COULD have stored
				 * literal type as a property, instead of using
				 */
				toType.setProperty(GraphSail.CONTEXT_PROP, GraphUtils.asSailProperty(GraphUtils.GAEDO_CONTEXT));
			}
			// Yup, this if has no default else statement, and that's normal.
			// returned.setProperty(Properties.value.name(), vertexId);
		} catch (RuntimeException e) {
			throw e;
		}

		return returned;
	}

	@Override
	protected String getEffectiveType(Vertex vertex) {
		if (vertex.getProperty(Properties.type.name()) != null) {
			return TypeUtils.getClass(vertex.getProperty(Properties.type.name()).toString());
		} else {
			Edge toType = vertex.getOutEdges(TYPE_EDGE_NAME).iterator().next();
			Vertex type = toType.getInVertex();
			// Do not use ClassLiteral here as this method must be blazing fast
			return classTransformer.extractClassIn(getValue(type).toString());
		}
	}

	/**
	 * There is no concept such as a value in a SailGraph
	 * @param vertex
	 * @param value
	 * @see com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService#setValue(com.tinkerpop.blueprints.pgm.Vertex, java.lang.Object)
	 */
	@Override
	protected void setValue(Vertex vertex, Object value) {
		Kind vertexKind = GraphUtils.getKindOf(vertex);
		switch(vertexKind) {
		case literal:
			vertex.setProperty(Properties.value.name(), value);
			break;
		case uri:
		case bnode:
		default:
		}
	}

	@Override
	protected Object getValue(Vertex vertex) {
		Kind vertexKind = GraphUtils.getKindOf(vertex);
		switch(vertexKind) {
		case literal:
			return vertex.getProperty(Properties.value.name());
		case bnode:
		case uri:
			return vertexKind.extractValueOf(vertex.getId().toString());
		default:
			return null;
		}
	}

	public Edge addEdgeFor(Vertex fromVertex, Vertex toVertex, Property property) {
		String edgeNameFor = GraphUtils.getEdgeNameFor(property);
		Edge edge = database.addEdge(getEdgeId(fromVertex, toVertex, property), fromVertex, toVertex, edgeNameFor);
		// edge.setProperty(GraphSail.PREDICATE_PROP,
		// GraphSail.URI_PREFIX+" "+GraphUtils.getDefaultEdgeNameFor(property));
		// Create a common context for all gaedo relationships
		// edge.setProperty(GraphSail.CONTEXT_PROP,
		// GraphUtils.asSailProperty(GraphUtils.GAEDO_CONTEXT));
		return edge;
	}

	public String getEdgeId(Vertex fromVertex, Vertex toVertex, Property property) {
		return fromVertex.getId().toString() + "_to_" + toVertex.getId().toString() + "___" + UUID.randomUUID().toString();
	}

}
