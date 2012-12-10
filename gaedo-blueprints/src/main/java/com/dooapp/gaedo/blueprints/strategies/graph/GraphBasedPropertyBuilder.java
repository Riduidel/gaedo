package com.dooapp.gaedo.blueprints.strategies.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.UnableToGetVertexTypeException;
import com.dooapp.gaedo.blueprints.transformers.TypeUtils;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

/**
 * Build property from informations given by output edges.
 * Notice we decide as a standard hypothesis that all graph properties are multivalued. There are NO single-value properties, even when, in fact, they are mono-valued.
 * It prevents the important risk of value type change during graph life : that one could have happen provided used data set was not clearly defined.
 * @author ndx
 *
 */
public class GraphBasedPropertyBuilder<DataType> {
	private List<Edge> correspondingEdges = new LinkedList<Edge>();

	private final GraphDatabaseDriver driver;

	/**
	 * Collection of named graphs which are REQUIRED in the {@link GraphSail#CONTEXT_PROP} of the edges
	 */
	private SortedSet<String> namedGraphs;

	private Class<DataType> serviceContainedClass;
	
	public GraphBasedPropertyBuilder(Class<DataType> serviceContainedClass, GraphDatabaseDriver driver, SortedSet<String> lens) {
		this.serviceContainedClass = serviceContainedClass;
		this.driver = driver;
		this.namedGraphs = lens;
	}

	public void add(Edge e) {
		correspondingEdges.add(e);
	}

	public Property build() {
		return buildCollection(correspondingEdges);
	}

	private Property buildCollection(List<Edge> correspondingEdges) {
		GraphProperty returned = new GraphProperty();
		String name = null;
		Set<String> targetTypes = new TreeSet<String>();
		Map<Object, AtomicLong> edgesPerInVertices = new HashMap<Object, AtomicLong>();
		for(Edge e : correspondingEdges) {
			name = e.getLabel();
			if(GraphUtils.isInNamedGraphs(e, namedGraphs)) {
				Vertex inVertex = e.getOutVertex();
				if(!edgesPerInVertices.containsKey(inVertex)) {
					edgesPerInVertices.put(inVertex, new AtomicLong(0));
				}
				edgesPerInVertices.get(inVertex).incrementAndGet();
				Vertex outVertex = e.getInVertex();
				try {
					targetTypes.add(driver.getEffectiveType(outVertex));
				} catch(UnableToGetVertexTypeException noType) {
					targetTypes.add(GraphMappingStrategy.STRING_TYPE);
				}
			}
		}
		if(edgesPerInVertices.size()==0)
			throw new NoEdgeInNamedGraphsException("no edge named \""+name+"\" is declared in named graphs "+namedGraphs);
		returned.setName(name);
		returned.setDeclaringClass(serviceContainedClass);
		String containedType = "Object";
		if(targetTypes.size()==1) {
			containedType = targetTypes.iterator().next();
		} else {
			// in any other case, we consider this collection to have only Objects as target
		}
		long maxCount = 0;
		for(AtomicLong v : edgesPerInVertices.values()) {
			maxCount = Math.max(maxCount, v.get());
		}
		returned.setType(List.class);
		// would have really loved to use generics, but it's a dead end (remember about generics reification ? fuck)
		returned.setGenericType(List.class);
		returned.setContainedTypeName(TypeUtils.getClass(containedType));
		returned.setAnnotation(new OneToManyGraph(serviceContainedClass));
		returned.setAnnotation(new GraphPropertyAnnotation(returned.getName(), PropertyMappingStrategy.asIs));
		return returned;
	}

//	private Property buildSingle(Edge edge) {
//		GraphProperty returned = new GraphProperty();
//		returned.setName(edge.getLabel());
//		if(!isInNamedGraphs(edge))
//			throw new NoEdgeInNamedGraphsException("no edge named \""+edge.getLabel()+"\" is declared in named graphs "+namedGraphs);
//		returned.setDeclaringClass(serviceContainedClass);
//		returned.setAnnotation(new OneToOneGraph(serviceContainedClass));
//		// named graphs in which this property is to be written are determined by the contextualized graph instance, not by this graph
//		returned.setAnnotation(new GraphPropertyAnnotation(edge.getLabel(), PropertyMappingStrategy.asIs));
//		String effectiveType = driver.getEffectiveType(edge.getInVertex());
//		returned.setTypeName(effectiveType);
//		returned.setGenericType(returned.getType());
//		return returned;
//	}

	
}