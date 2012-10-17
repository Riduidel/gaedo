package com.dooapp.gaedo.blueprints.sail;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.transformers.ClassLiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

public class SailGraphBackedFinderService<DataType, InformerType extends Informer<DataType>> extends AbstractBluePrintsBackedFinderService<SailGraph, DataType, Informer<DataType>>{

	private static final String GAEDO = "gaedo";

	public static final String TYPE_EDGE_NAME = GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE);

	private ClassLiteralTransformer classTransformer = (ClassLiteralTransformer) Literals.get(Class.class);


	public SailGraphBackedFinderService(Class<DataType> containedClass, Class<Informer<DataType>> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, SailGraph graph) {
		super(graph,containedClass, informerClass, factory, repository, provider);
		// at creation, make sure graph supports the gaedo prefix
		graph.addNamespace(GAEDO, GraphUtils.GAEDO_CONTEXT);
	}

	@Override
	public Vertex loadVertexFor(String objectVertexId, String className) {
		return database.getVertex(objectVertexId);
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
			Edge toType = GraphUtils.addEdgeFor(getDriver(), database, returned, classVertex, TypeProperty.INSTANCE);
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
		if(vertex.getProperty(Properties.type.name())!=null) {
			return TypeUtils.getClass(vertex.getProperty(Properties.type.name()).toString());
		} else {
			Edge toType = vertex.getOutEdges(TYPE_EDGE_NAME).iterator().next();
			Vertex type = toType.getInVertex();
			// Do not use ClassLiteral here as this method must be blazing fast
			return classTransformer.extractClassIn(getValue(type).toString());
		}
	}

	@Override
	protected void setValue(Vertex vertex, Object value) {
		vertex.setProperty(Properties.value.name(), value);
	}

	@Override
	protected Object getValue(Vertex vertex) {
		return vertex.getProperty(Properties.value.name());
	}

}
