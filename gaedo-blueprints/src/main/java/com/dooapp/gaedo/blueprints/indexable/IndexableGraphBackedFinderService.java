package com.dooapp.gaedo.blueprints.indexable;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.blueprints.strategies.UnableToGetVertexTypeException;
import com.dooapp.gaedo.blueprints.transformers.ClassLiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;

/**
 * Indexable graph backed version of finder service.
 *
 * Notice we maintain {@link AbstractCooperantFinderService} infos about objects
 * being accessed as String containing, in fact, vertex ids.
 *
 * Since Blueprints 2.*, there is a distinction between manual indices (provided by IndexableGraph) and automatic key indices, provided by KeyIndexableGraph. Why using the first ones ?
 * For queries, obviously : an {@link Index} can return the {@link Index#count(String, Object)} number of element, what a KeyIndexableGraph
 * do not yet provide. And, to have a good execution plan (without relying upon graph queries), this is invaluable.
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

	private static final Logger logger = Logger.getLogger(IndexableGraphBackedFinderService.class.getName());

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
		loadIndices(graph);
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
		loadIndices(graph);
	}

	public IndexableGraphBackedFinderService(IndexableGraph graph, Class<DataType> containedClass, Class<InformerType> informerClass, InformerFactory factory,
					ServiceRepository repository, PropertyProvider provider) {
		super(graph, containedClass, informerClass, factory, repository, provider);
		loadIndices(graph);
	}

	/**
	 * There is no automatic index creation !
	 * No worry, we will add them by hand
	 * @param graph
	 */
	private void loadIndices(IndexableGraph graph) {
		for(IndexNames index : IndexNames.values()) {
			Index<? extends Element> associatedIndex = graph.getIndex(index.getIndexName(), index.getIndexed());
			if(associatedIndex==null) {
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "There were no indice "+index.describe()+". Creating it");
				}
				graph.createIndex(index.getIndexName(), index.getIndexed());
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "index "+index.describe()+" has been created");
				}
			}
		}
	}

	@Override
	public Vertex loadVertexFor(String objectVertexId, String className) {
		Vertex defaultVertex = null;
		CloseableIterable<Vertex> matchingIterable = database.getIndex(IndexNames.VERTICES.getIndexName(), Vertex.class).get(Properties.value.name(), objectVertexId);
		Iterator<Vertex> matching = matchingIterable.iterator();
		if (matching.hasNext()) {
			while (matching.hasNext()) {
				Vertex vertex = matching.next();
				String vertexTypeName = null;
				try {
					vertexTypeName = getEffectiveType(vertex);
					switch (GraphUtils.getKindOf(vertex)) {
					case literal:
					case bnode:
					case uri:
						if (className.equals(vertexTypeName)) {
							return vertex;
						}
						break;
					default:
						return vertex;
					}
				} catch(UnableToGetVertexTypeException e) {
					if(GraphMappingStrategy.STRING_TYPE.equals(className)) {
						/* in that very case, we can use a type-less vertex as our result */
						defaultVertex = vertex;
					} else {
						if(Kind.uri==GraphUtils.getKindOf(vertex))
							defaultVertex = vertex;
					}
				}
			}
		}
        return defaultVertex;
	}

	@Override
	public String getIdOfVertex(Vertex objectVertex) {
		return objectVertex.getProperty(Properties.value.name()).toString();
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
		Vertex returned = database.addVertex(valueClass.getName() + ":" + vertexId);
		setIndexedProperty(returned, Properties.value.name(), vertexId, IndexNames.VERTICES);
		if (Literals.containsKey(valueClass)) {
			// some literals aren't so ... literal, as they can accept incoming
			// connections (like classes)
			setIndexedProperty(returned, Properties.kind.name(), Literals.get(valueClass).getKind().name(), IndexNames.VERTICES);
			setIndexedProperty(returned, Properties.type.name(), Literals.get(valueClass).getTypeOf(value), IndexNames.VERTICES);
		} else {
			if (repository.containsKey(valueClass)) {
				setIndexedProperty(returned, Properties.kind.name(), Kind.uri.name(), IndexNames.VERTICES);
			} else if (Tuples.containsKey(valueClass)) {
				// some literals aren't so ... literal, as they can accept
				// incoming connections (like classes)
				setIndexedProperty(returned, Properties.kind.name(), Tuples.get(valueClass).getKind().name(), IndexNames.VERTICES);
			}
			// obtain vertex for type
			Vertex classVertex = classTransformer.getVertexFor(getDriver(), valueClass, CascadeType.PERSIST);
			Edge toType = getDriver().createEdgeFor(returned, classVertex, TypeProperty.INSTANCE);
			/*
			 * Make sure literals are literals by changing that particular edge
			 * context to a null value. Notice we COULD have stored literal type
			 * as a property, instead of using
			 */
			setIndexedProperty(toType, GraphUtils.CONTEXT_PROPERTY, GraphUtils.asSailProperty(GraphUtils.GAEDO_CONTEXT), IndexNames.EDGES);
		}
		// Yup, this if has no default else statement, and that's normal.
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "created vertex "+GraphUtils.toString(returned));
		}
		return returned;
	}

	@Override
	protected String getEffectiveType(Vertex vertex) {
		return getStrategy().getEffectiveType(vertex);
	}

	@Override
	protected void setValue(Vertex vertex, Object value) {
		setIndexedProperty(vertex, Properties.value.name(), value, IndexNames.VERTICES);
	}

	@Override
	protected Object getValue(Vertex vertex) {
		return vertex.getProperty(Properties.value.name());
	}

	public Edge createEdgeFor(Vertex fromVertex, Vertex toVertex, Property property) {
		String edgeNameFor = GraphUtils.getEdgeNameFor(property);
		Edge returned = database.addEdge(getEdgeId(fromVertex, toVertex, property), fromVertex, toVertex, edgeNameFor);
		// Did you know labels are not edges properties ? Absolutely stunning discovery !
//		database.getIndex(IndexNames.EDGES.getIndexName(), Edge.class).put("label", edgeNameFor, returned);
		String predicateProperty = GraphUtils.asSailProperty(GraphUtils.getEdgeNameFor(property));
		setIndexedProperty(returned, GraphUtils.PREDICATE_PROPERTY, predicateProperty, IndexNames.EDGES);
		Collection<String> contexts = getLens();
		StringBuilder contextPropertyBuilder = new StringBuilder();
		if (contexts.size() == 0) {
			contextPropertyBuilder.append(GraphUtils.asSailProperty(GraphSail.NULL_CONTEXT_NATIVE));
		} else {
			for (String context : contexts) {
				if (contextPropertyBuilder.length() > 0)
					contextPropertyBuilder.append(" ");
				contextPropertyBuilder.append(GraphUtils.asSailProperty(context));

			}
		}
		String contextProperty = contextPropertyBuilder.toString();
		setIndexedProperty(returned, GraphUtils.CONTEXT_PROPERTY, contextProperty, IndexNames.EDGES);
		// Finally build the context-predicate property by concatenating both
		setIndexedProperty(returned, GraphUtils.CONTEXT_PREDICATE_PROPERTY, contextProperty + " " + predicateProperty, IndexNames.EDGES);

		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "created edge "+GraphUtils.toString(returned));
		}
		return returned;
	}

	/**
	 * Set an indexed property on any graph element, updating the given list of indices
	 * @param graphElement
	 * @param propertyName
	 * @param propertyValue
	 * @param indexName
	 */
	public <ElementType extends Element> void setIndexedProperty(ElementType graphElement, String propertyName, Object propertyValue, IndexNames indexName) {
		GraphUtils.setIndexedProperty(database, graphElement, propertyName, propertyValue, indexName);
	}

	public String getEdgeId(Vertex fromVertex, Vertex toVertex, Property property) {
		return fromVertex.getId().toString() + "_to_" + toVertex.getId().toString() + "___" + UUID.randomUUID().toString();
	}

	@Override
	public InViewService<DataType, InformerType, SortedSet<String>> focusOn(SortedSet<String> lens) {
		AbstractBluePrintsBackedFinderService<IndexableGraph, DataType, InformerType> returned = new IndexableGraphBackedFinderService<DataType, InformerType>(
						database, containedClass, informerClass, getInformerFactory(), repository, propertyProvider,
						/* strategy is local to service ! */ getStrategy().derive());
		returned.setLens(lens);
		return returned;
	}

}
