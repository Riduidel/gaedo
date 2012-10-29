package com.dooapp.gaedo.blueprints.strategies.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Build property from informations given by output edges
 * @author ndx
 *
 */
public class GraphBasedPropertyBuilder<DataType> {
	private List<Edge> correspondingEdges = new LinkedList<Edge>();
	
	private Class<DataType> serviceContainedClass;

	private final GraphDatabaseDriver driver;
	
	public GraphBasedPropertyBuilder(Class<DataType> serviceContainedClass, GraphDatabaseDriver driver) {
		this.serviceContainedClass = serviceContainedClass;
		this.driver = driver;
	}

	public void add(Edge e) {
		correspondingEdges.add(e);
	}

	public Property build() {
		if(correspondingEdges.size()==1)
			return buildSingle(correspondingEdges.get(0));
		else if(correspondingEdges.size()>1)
			return buildCollection(correspondingEdges);
		else
			throw new UnsupportedOperationException();
	}

	private Property buildCollection(List<Edge> correspondingEdges) {
		GraphProperty returned = new GraphProperty();
		String name = null;
		Set<String> targetTypes = new TreeSet<String>();
		Map<Object, AtomicLong> edgesPerInVertices = new HashMap<Object, AtomicLong>();
		for(Edge e : correspondingEdges) {
			name = e.getLabel();
			Vertex inVertex = e.getOutVertex();
			if(!edgesPerInVertices.containsKey(inVertex)) {
				edgesPerInVertices.put(inVertex, new AtomicLong(0));
			}
			edgesPerInVertices.get(inVertex).incrementAndGet();
			Vertex outVertex = e.getInVertex();
			targetTypes.add(driver.getEffectiveType(outVertex));
		}
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
		if(maxCount>1) {
			returned.setType(List.class);
			// would have really loved to use generics, but it's a dead end (remember about generics reification ? fuck)
			returned.setGenericType(List.class);
			returned.setContainedTypeName(containedType);
			returned.setAnnotation(new OneToManyGraph(serviceContainedClass));
		} else {
			returned.setTypeName(containedType);
			returned.setGenericType(returned.getType());
		}
		return returned;
	}

	private Property buildSingle(Edge edge) {
		GraphProperty returned = new GraphProperty();
		returned.setName(edge.getLabel());
		returned.setDeclaringClass(serviceContainedClass);
		returned.setAnnotation(new OneToOneGraph(serviceContainedClass));
		String effectiveType = driver.getEffectiveType(edge.getInVertex());
		returned.setTypeName(effectiveType);
		returned.setGenericType(returned.getType());
		return returned;
	}
	
}