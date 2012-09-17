package com.dooapp.gaedo.blueprints.indexable;


import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.IndexableGraphQueryStatement;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
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
	
	/**
	 * property identifiying in an unique way a vertex. Its goal is to make sure vertex is the one we search.
	 * @deprecated should be replaced by a "tagged" edge linking object to its identifying property
	 */
	private static final String VERTEX_ID = "vertexId";

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

	@Override
	protected Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass) {
		Vertex returned = database.addVertex(vertexId);
		returned.setProperty(VERTEX_ID, vertexId);
		returned.setProperty(Properties.type.name(), valueClass.getName());
		return returned;
	}

}
