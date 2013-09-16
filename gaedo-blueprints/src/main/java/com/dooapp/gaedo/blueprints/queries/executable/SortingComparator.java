package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.exceptions.UncomparableObjectsInSortingException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.SortingExpression.Direction;
import com.dooapp.gaedo.finders.sort.SortingExpressionVisitor;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class SortingComparator implements Comparator<Vertex> {
	private class SortedPropertiesSubGraph {

		/**
		 * Vertex corresponding to object we want to load properties of
		 */
		private Vertex vertex;
		/**
		 * Map linking FieldInformer to values loaded by navigating from initial {@link #vertex}
		 */
		private Map<FieldInformer, Object> fieldValues = new HashMap<FieldInformer, Object>();

		public SortedPropertiesSubGraph(Vertex vertex) {
			this.vertex = vertex;
		}

		public Object get(FieldInformer<?> key) {
			if(!fieldValues.containsKey(key)) {
				Vertex currentVertex = vertex;
				Object value = null;
				for(Property currentProperty : key.getFieldPath()) {
					Iterator<Edge> edges = service.getStrategy().getOutEdgesFor(currentVertex, currentProperty).iterator();
					if(edges.hasNext()) {
						currentVertex = edges.next().getVertex(com.tinkerpop.blueprints.Direction.IN);
					} else {
						// no navigable value ? Then null is the bad result we're looking for
						currentVertex = null;
					}
				}
				if(currentVertex!=null) {
					value = service.getDriver().getValue(currentVertex);
				}
				fieldValues.put(key, value);
			}
			return fieldValues.get(key);
		}

	}

	private class ComparatorVisitor implements SortingExpressionVisitor {

		private SortedPropertiesSubGraph firstSubGraph;
		private SortedPropertiesSubGraph secondSubGraph;

		/**
		 * Final comparison result. The whole goal here is to find that result.
		 */
		private int result = 0;

		public ComparatorVisitor(SortedPropertiesSubGraph firstSubGraph, SortedPropertiesSubGraph secondSubGraph) {
			this.firstSubGraph = firstSubGraph;
			this.secondSubGraph = secondSubGraph;
		}

		public int result() {
			return result;
		}

		@Override
		public void startVisit(SortingExpression sortingExpression) {
		}

		@Override
		public void endVisit(SortingExpression sortingExpression) {
			// a trick to force all vertices being loaded (even when equals)
			if(result==0)
				result = firstSubGraph.vertex.getId().toString().compareTo(secondSubGraph.vertex.getId().toString());
		}

		/**
		 * For each entry, we have to first check if a result has already been obtained. If not, we load properties on both subgraph and then try to comapre them (they should be comparable)
		 * @param entry
		 * @see com.dooapp.gaedo.finders.sort.SortingExpressionVisitor#visit(java.util.Map.Entry)
		 */
		@Override
		public void visit(Entry<FieldInformer, Direction> entry) {
			if(result==0) {
				Object firstValue = firstSubGraph.get(entry.getKey());
				Object secondValue = secondSubGraph.get(entry.getKey());
				try {
					result = entry.getValue().compareTo(firstValue, secondValue);
				} catch(UncomparableObjectsInSortingException e) {

				}
			}
		}

	}

	private AbstractBluePrintsBackedFinderService<?, ?, ?> service;

	/**
	 * A very complicated object associating a vertex to an object that contains properties, sorted in order defined by {@link SortingExpression}
	 * and allowing easy test in compare by simply
	 */
	private Map<Vertex, SortedPropertiesSubGraph> sortedProperties = new LinkedHashMap<Vertex, SortedPropertiesSubGraph>();

	/**
	 * Sorting expression used to visit {@link SortedPropertiesSubGraph} associated to vertices
	 */
	private SortingExpression sort;

	/**
	 * Construct the good comparator
	 * @param service service defines the mapping strategy and the way edges can be navigated
	 * @param sort sorting expression define which edges will be navigated
	 */
	public SortingComparator(AbstractBluePrintsBackedFinderService<?, ?, ?> service, SortingExpression sort) {
		this.service = service;
		this.sort = sort;
	}

	/**
	 * Compare vertices by navigating edges associated to properties expressed in SortingExpression.
	 * Notice that if all matches, vertices are tested on their id (if comparable)
	 * @param firstVertex
	 * @param secondVertex
	 * @return
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Vertex firstVertex, Vertex secondVertex) {
		SortedPropertiesSubGraph firstSubGraph = getSubGraphOf(firstVertex);
		SortedPropertiesSubGraph secondSubGraph = getSubGraphOf(secondVertex);
		ComparatorVisitor visitor = new ComparatorVisitor(firstSubGraph, secondSubGraph);
		sort.accept(visitor);
		return visitor.result();
	}

	/**
	 * Get or load subgraph for given vertex
	 * @param vertex
	 * @return a SortedPropertiesSubGraph for vertex
	 */
	private SortedPropertiesSubGraph getSubGraphOf(Vertex vertex) {
		if(!sortedProperties.containsKey(vertex)) {
			sortedProperties.put(vertex, createSortedPropertiesOf(vertex));
		}
		return sortedProperties.get(vertex);
	}

	/**
	 * Create properties subgraph by navigating around vertex. Well ... not really as it is a lazy loaded structure
	 * @param vertex
	 * @return
	 */
	private SortedPropertiesSubGraph createSortedPropertiesOf(Vertex vertex) {
		return new SortedPropertiesSubGraph(vertex);
	}

}
