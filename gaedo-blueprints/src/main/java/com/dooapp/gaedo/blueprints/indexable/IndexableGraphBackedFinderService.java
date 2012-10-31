package com.dooapp.gaedo.blueprints.indexable;


import java.util.UUID;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.annotations.GraphProperty;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.blueprints.transformers.ClassLiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

/**
 * Indexable graph backed version of finder service.
 * 
 * Notice we maintain {@link AbstractCooperantFinderService} infos about objects being accessed as String containing, in fact, vertex ids
 * @author ndx
 *
 * @param <DataType> type of data managed by this service
 * @param <InformerType> type of informer used to provide infos about managed data
 */
public class IndexableGraphBackedFinderService <DataType, InformerType extends Informer<DataType>> 
	extends AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, InformerType> {

	public static final String TYPE_EDGE_NAME = GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE);

	public static ClassLiteralTransformer classTransformer = (ClassLiteralTransformer) Literals.get(Class.class);

	public IndexableGraphBackedFinderService(Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory, ServiceRepository repository,
					PropertyProvider provider, IndexableGraph graph) {
		this(graph, containedClass, informerClass, factory, repository, provider, StrategyType.beanBased);
	}
	
	

	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, GraphMappingStrategy<DataType> strategy) {
		super(graph, containedClass, informerClass, factory, repository, provider, strategy);
	}



	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, StrategyType strategy) {
		super(graph, containedClass, informerClass, factory, repository, provider, strategy);
	}



	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider) {
		super(graph, containedClass, informerClass, factory, repository, provider);
	}



	@Override
	public Vertex loadVertexFor(String objectVertexId, String className) {
		CloseableSequence<Vertex> matching = database.getIndex(Index.VERTICES, Vertex.class).get(Properties.value.name(), objectVertexId);
		if(matching.hasNext()) {
			while(matching.hasNext()) {
				Vertex vertex = matching.next();
				String vertexTypeName = getEffectiveType(vertex);
				if(Kind.literal==GraphUtils.getKindOf(vertex)) {
					if(className.equals(vertexTypeName)) {
						return vertex;
					}
				} else {
					return vertex;
				}
			}
		}
		return null;
	}

	@Override
	public String getIdOfVertex(Vertex objectVertex) {
		return objectVertex.getProperty(Properties.value.name()).toString();
	}

	/**
	 * When creating an empty vertex, we immediatly link it to its associated type vertex : a long will as a consequence be linked to the Long class
	 * @param vertexId
	 * @param valueClass
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService#createEmptyVertex(java.lang.String, java.lang.Class)
	 */
	@Override
	protected Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass) {
		// technical vertex id is no more used by gaedo which only rley upon the getIdOfVertex method !
		Vertex returned = database.addVertex(valueClass.getName()+":"+vertexId);
		returned.setProperty(Properties.value.name(), vertexId);
		if(Literals.containsKey(valueClass)) {
			// some literals aren't so ... literal, as they can accept incoming connections (like classes)
			returned.setProperty(Properties.kind.name(), Literals.get(valueClass).getKind().name());
			returned.setProperty(Properties.type.name(), TypeUtils.getType(valueClass));
		} else {
			if(repository.containsKey(valueClass)){
				returned.setProperty(Properties.kind.name(), Kind.uri.name());
			} else if(Tuples.containsKey(valueClass)) {
				// some literals aren't so ... literal, as they can accept incoming connections (like classes)
				returned.setProperty(Properties.kind.name(), Tuples.get(valueClass).getKind().name());
			}
			// obtain vertex for type
			Vertex classVertex = classTransformer.getVertexFor(getDriver(), valueClass);
			Edge toType = getDriver().addEdgeFor(returned, classVertex, TypeProperty.INSTANCE);
			/*
			 * Make sure literals are literals by changing that particular edge context to a null value.
			 *  Notice we COULD have stored literal type as a property, instead of using
			 */
			toType.setProperty(GraphSail.CONTEXT_PROP, GraphUtils.asSailProperty(GraphUtils.GAEDO_CONTEXT));
		}
		// Yup, this if has no default else statement, and that's normal.
		
		return returned;
	}

	@Override
	protected String getEffectiveType(Vertex vertex) {
		return getStrategy().getEffectiveType(vertex);
	}

	@Override
	protected void setValue(Vertex vertex, Object value) {
		vertex.setProperty(Properties.value.name(), value);
	}

	@Override
	protected Object getValue(Vertex vertex) {
		return vertex.getProperty(Properties.value.name());
	}

	public Edge addEdgeFor(Vertex fromVertex, Vertex toVertex, Property property) {
		String edgeNameFor = GraphUtils.getEdgeNameFor(property);
		Edge edge = database.addEdge(getEdgeId(fromVertex, toVertex, property), fromVertex, toVertex, edgeNameFor);
		String predicateProperty = GraphUtils.asSailProperty(GraphUtils.getEdgeNameFor(property));
		edge.setProperty(GraphSail.PREDICATE_PROP, predicateProperty);
		String[] contexts = new String[] { GraphUtils.GAEDO_CONTEXT };
		if(property.getAnnotation(GraphProperty.class)!=null) {
			GraphProperty graph = property.getAnnotation(GraphProperty.class); 
			contexts = graph.contexts();
		}
		StringBuilder contextPropertyBuilder = new StringBuilder();
		for(String context : contexts) {
			contextPropertyBuilder.append(GraphUtils.asSailProperty(context)).append(" ");
		}
		String contextProperty = contextPropertyBuilder.toString();
		edge.setProperty(GraphSail.CONTEXT_PROP, contextProperty);
		// Finally build the context-predicate property by concatenating both
		edge.setProperty(GraphSail.CONTEXT_PROP + GraphSail.PREDICATE_PROP, contextProperty+" "+predicateProperty);
		return edge;
	}

	public String getEdgeId(Vertex fromVertex, Vertex toVertex, Property property) {
		return fromVertex.getId().toString()+"_to_"+toVertex.getId().toString()+"___"+UUID.randomUUID().toString(); 
	}

}
