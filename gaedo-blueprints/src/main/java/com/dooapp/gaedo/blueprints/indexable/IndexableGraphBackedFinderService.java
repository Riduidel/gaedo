package com.dooapp.gaedo.blueprints.indexable;


import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.IndexableGraphQueryStatement;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Indexable graph backed version of finder service.
 * 
 * Notice we maintain {@link AbstractCooperantFinderService} infos about objects being accessed as String containing, in fact, vertex ids
 * @author ndx
 *
 */
public class IndexableGraphBackedFinderService <DataType, InformerType extends Informer<DataType>> 
	extends AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, InformerType> {
	private static final String VALUE = "value";

	/**
	 * property identifiying in an unique way a vertex. Its goal is to make sure vertex is the one we search.
	 * @deprecated should be replaced by a "tagged" edge linking object to its identifying property
	 */
	private static final String VERTEX_ID = "vertexId";
	
	public static final String TYPE_EDGE_NAME = GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE);

	private LiteralTransformer<Class> classTransformer = Literals.get(Class.class);

	public IndexableGraphBackedFinderService(Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory, ServiceRepository repository,
					PropertyProvider provider, IndexableGraph graph) {
		super(graph, containedClass, informerClass, factory, repository, provider);
	}

	@Override
	protected QueryStatement<DataType, InformerType> createQueryStatement(QueryBuilder<InformerType> query) {
		return new IndexableGraphQueryStatement<DataType, InformerType>(query,
						this, database, repository);
	}

	@Override
	public Vertex loadVertexFor(String objectVertexId) {
		CloseableSequence<Vertex> matching = database.getIndex(Index.VERTICES, Vertex.class).get(VERTEX_ID, objectVertexId);
		if(matching.hasNext()) {
			return matching.next();
		} else {
			return null;
		}
	}

	@Override
	public String getIdOfVertex(Vertex objectVertex) {
		return objectVertex.getProperty(VERTEX_ID).toString();
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
		Vertex returned = database.addVertex(vertexId);
		returned.setProperty(VERTEX_ID, vertexId);
		// obtain vertex for type
		Vertex classVertex = classTransformer.getVertexFor(getDriver(), valueClass);
		database.addEdge("automatic edge linking "+returned.getId().toString()+" to its class "+valueClass.getName(), /* from */ returned, /* to */ classVertex, TYPE_EDGE_NAME);
		return returned;
	}

	@Override
	protected String getEffectiveType(Vertex vertex) {
		Edge toType = vertex.getOutEdges(TYPE_EDGE_NAME).iterator().next();
		Vertex type = toType.getInVertex();
		// Do not use ClassLiteral here as this method must be blazing fast
		return getValue(type).toString();
	}

	@Override
	protected void setValue(Vertex vertex, Object value) {
		vertex.setProperty(VALUE, value);
	}

	@Override
	protected Object getValue(Vertex vertex) {
		return vertex.getProperty(VALUE);
	}

}
