package com.dooapp.gaedo.blueprints.strategies.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;
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
	/**
	 * Compiled pattern used to match strings such as 
	 * <pre>U https://github.com/Riduidel/gaedo/visible  U http://purl.org/dc/elements/1.1/description</pre>
	 * or
	 * <pre>N U http://purl.org/dc/elements/1.1/description</pre>
	 * or even 
	 * <pre>N</pre>
	 * 
	 * You know why I do such a pattern matching ? Because sail graph named graph definintion goes by concatenaing contexts URI in edges properties.
	 * This is really douchebag code !
	 */
	private static final Pattern CONTEXTS_MATCHER = Pattern.compile("(N|U ([\\S]+))+");
	
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
			if(isInNamedGraphs(e)) {
				Vertex inVertex = e.getOutVertex();
				if(!edgesPerInVertices.containsKey(inVertex)) {
					edgesPerInVertices.put(inVertex, new AtomicLong(0));
				}
				edgesPerInVertices.get(inVertex).incrementAndGet();
				Vertex outVertex = e.getInVertex();
				targetTypes.add(driver.getEffectiveType(outVertex));
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
		returned.setContainedTypeName(containedType);
		returned.setAnnotation(new OneToManyGraph(serviceContainedClass));
		returned.setAnnotation(new GraphPropertyAnnotation(returned.getName(), PropertyMappingStrategy.asIs));
		return returned;
	}

	private boolean isInNamedGraphs(Edge e) {
		Collection<String> contexts = getContextsOf(e);
		// Only analyse edge if it is in named graph, and only in named graphs
		boolean isInNamedGraphs = contexts.size()==namedGraphs.size() && contexts.containsAll(namedGraphs);
		return isInNamedGraphs;
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

	/**
	 * Find all contexts in given edge by looking, in {@link GraphSail#CONTEXT_PROP} property, what are the contexts. These contexts are extracted by iteratively calling 
	 * {@link #CONTEXTS_MATCHER} and {@link Matcher#find(int)} method.
	 * @param edge input edge
	 * @return collection of declared contexts.
	 */
	private Collection<String> getContextsOf(Edge edge) {
		String contextsString = edge.getProperty(GraphSail.CONTEXT_PROP).toString();
		Matcher matcher = CONTEXTS_MATCHER.matcher(contextsString);
		Collection<String> output = new LinkedList<String>();
		int character = 0;
		while(matcher.find(character)) {
			if(GraphSail.NULL_CONTEXT_NATIVE.equals(matcher.group(1))) {
				// the null context is a low-level view. It should be associated with "no named graph" (that's to say an empty collection).
				return output;
			} else if(matcher.group(1).startsWith(GraphSail.URI_PREFIX+"")){
				output.add(matcher.group(2));
			}
			character = matcher.end();
		}
		return output;
	}
	
}