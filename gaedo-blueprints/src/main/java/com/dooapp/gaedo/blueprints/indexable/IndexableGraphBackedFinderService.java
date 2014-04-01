package com.dooapp.gaedo.blueprints.indexable;

import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.VertexHasNoPropertyException;
import com.dooapp.gaedo.blueprints.indexable.IndexBrowser.VertexMatcher;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.blueprints.transformers.ClassLiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Indexable graph backed version of finder service.
 *
 * Notice we maintain {@link AbstractCooperantFinderService} infos about objects
 * being accessed as String containing, in fact, vertex ids.
 *
 * Since Blueprints 2.*, there is a distinction between manual indices (provided
 * by IndexableGraph) and automatic key indices, provided by KeyIndexableGraph.
 * Why using the first ones ? For queries, obviously : an {@link Index} can
 * return the {@link Index#count(String, Object)} number of element, what a
 * KeyIndexableGraph do not yet provide. And, to have a good execution plan
 * (without relying upon graph queries), this is invaluable.
 *
 * @author ndx
 *
 * @param <DataType>
 *            type of data managed by this service
 * @param <InformerType>
 *            type of informer used to provide infos about managed data
 */
public class IndexableGraphBackedFinderService<DataType, InformerType extends Informer<DataType>> extends
				AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, InformerType> {

	private class IndexableVertexMatcher implements VertexMatcher {
		@Override
		public String getTypeOf(Vertex vertex) {
			return getEffectiveType(vertex);
		}

		@Override
		public Kind getKindOf(Vertex vertex) {
			return GraphUtils.getKindOf(vertex);
		}

		@Override
		public String getIdOf(Vertex vertex) {
			return getIdOfVertex(vertex);
		}
	}

	public static final Logger logger = Logger.getLogger(IndexableGraphBackedFinderService.class.getName());

	public static final String TYPE_EDGE_NAME = GraphUtils.getEdgeNameFor(TypeProperty.INSTANCE);

	public static ClassLiteralTransformer classTransformer = (ClassLiteralTransformer) Literals.get(Class.class);

	/**
	 * Construct a default service, for which the mapping strategy is the
	 * default one (that's to say {@link StrategyType#beanBased}
	 *
	 * @param containedClass
	 *            contained data class
	 * @param informerClass
	 *            informer calss associated to that data class
	 * @param factory
	 *            informer factory used when performing queries
	 * @param repository
	 *            service repository used to load other classes
	 * @param provider
	 *            property provider
	 * @param graph
	 *            graph used as storage
	 * @see IndexableGraphBackedFinderService#IndexableGraphBackedFinderService(IndexableGraph,
	 *      Class, Class, InformerFactory, ServiceRepository, PropertyProvider,
	 *      StrategyType)
	 * @deprecated replaced by
	 *             {@link IndexableGraphBackedFinderService#IndexableGraphBackedFinderService(IndexableGraph, Class, Class, InformerFactory, ServiceRepository, PropertyProvider)}
	 */
	public IndexableGraphBackedFinderService(Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, IndexableGraph graph) {
		this(graph, containedClass, informerClass, factory, repository, provider, StrategyType.beanBased);
	}

	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, GraphMappingStrategy<DataType> strategy) {
		super(graph, containedClass, informerClass, factory, repository, provider, strategy);
		prepareGraph(getDatabase());
	}

	/**
	 * Construct a gaedo servcie allowing reading/writing to an indexable graph
	 *
	 * @param graph
	 *            graph we want to write/read to/from
	 * @param containedClass
	 *            class we want to map to that graph
	 * @param informerClass
	 *            informer used to allow easy queries on that class
	 * @param factory
	 *            informer factory
	 * @param repository
	 *            service repository, to load other classes
	 * @param provider
	 *            property provider
	 * @param strategy
	 *            mapping strategy. If bean based, the bean fields will define
	 *            which edges are read/written. If graph based, that's the edges
	 *            that define how the object will be loaded.
	 */
	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider, StrategyType strategy) {
		super(graph, containedClass, informerClass, factory, repository, provider, strategy);
		prepareGraph(getDatabase());
	}

	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider) {
		super(graph, containedClass, informerClass, factory, repository, provider);
		prepareGraph(getDatabase());
	}

	/**
	 * There is no automatic index creation ! No worry, we will add them by hand
	 *
	 * @param graph
	 */
	private void prepareGraph(IndexableGraph graph) {
		// load indices
		for (IndexNames index : IndexNames.values()) {
			Index<? extends Element> associatedIndex = graph.getIndex(index.getIndexName(), index.getIndexed());
			if (associatedIndex == null) {
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "There were no indice " + index.describe() + ". Creating it");
				}
				graph.createIndex(index.getIndexName(), index.getIndexed());
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "index " + index.describe() + " has been created");
				}
			}
		}
	}

	@Override
	public Vertex loadVertexFor(String objectVertexId, String className) {
		return new IndexBrowser().browseFor(getDatabase(), objectVertexId, className, vertexMatcher());
	}

	public VertexMatcher vertexMatcher() {
		return new IndexableVertexMatcher();
	}

	@Override
	public String getIdOfVertex(Vertex objectVertex) {
		Object value = objectVertex.getProperty(Properties.value.name());
		if (value == null) {
			throw new VertexHasNoPropertyException("vertex " + GraphUtils.toString(objectVertex) + " has no " + Properties.value + " property defined.");
		}
		return value.toString();
	}

	/**
	 * When creating an empty vertex, we immediatly link it to its associated
	 * type vertex : a long will as a consequence be linked to the Long class
	 *
	 * @param vertexId
	 * @param valueClass
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService#createEmptyVertex(java.lang.String,
	 *      java.lang.Class, Object)
	 */
	@Override
	protected Vertex createEmptyVertex(String vertexId, Class<? extends Object> valueClass, Object value) {
		// technical vertex id is no more used by gaedo which only rley upon the
		// getIdOfVertex method !
		return GraphUtils.createEmptyVertexFor(getDatabase(), repository, vertexId, valueClass, value);
	}

	@Override
	protected String getEffectiveType(Vertex vertex) {
		return getStrategy().getEffectiveType(vertex);
	}

	@Override
	protected void setValue(Vertex vertex, Object value) {
		GraphUtils.setIndexedProperty(database, vertex, Properties.value.name(), value);
	}

	@Override
	protected Object getValue(Vertex vertex) {
		return vertex.getProperty(Properties.value.name());
	}

	public Edge createEdgeFor(Vertex fromVertex, Vertex toVertex, Property property) {
		String edgeNameFor = GraphUtils.getEdgeNameFor(property);
		Edge returned = database.addEdge(getEdgeId(fromVertex, toVertex, property), fromVertex, toVertex, edgeNameFor);
		// Did you know labels are not edges properties ? Absolutely stunning
		// discovery !
		// database.getIndex(IndexNames.EDGES.getIndexName(),
		// Edge.class).put("label", edgeNameFor, returned);

		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "created edge " + GraphUtils.toString(returned));
		}
		return returned;
	}

	public String getEdgeId(Vertex fromVertex, Vertex toVertex, Property property) {
		return fromVertex.getId().toString() + "_to_" + toVertex.getId().toString() + "___" + UUID.randomUUID().toString();
	}

	@Override
	public InViewService<DataType, InformerType, SortedSet<String>> focusOn(SortedSet<String> lens) {
		AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, InformerType> returned = new IndexableGraphBackedFinderService<DataType, InformerType>(
						getDatabase(), containedClass, informerClass, getInformerFactory(), repository, propertyProvider,
						/* strategy is local to service ! */getStrategy().derive());
		returned.setLens(lens);
		return returned;
	}
}
