package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexNames;
import com.dooapp.gaedo.blueprints.operations.LiteralInCollectionUpdaterProperty;
import com.dooapp.gaedo.blueprints.operations.Updater;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.EqualsTo;
import com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;

/**
 * Build a sorted set of {@link VertexSet} objects. Each of these sets
 *
 * @author ndx
 *
 */
public class VertexRootsCollector extends VertexTestVisitorAdapter {
	private final class IndexLazyLoader implements LazyLoader {
		private final Index<Vertex> vertices;
		private final String propertyKeyInIndex;
		private final String propertyValueInIndex;

		public IndexLazyLoader(Index<Vertex> vertices, String propertyKeyInIndex, String propertyValueInIndex) {
			super();
			this.propertyValueInIndex = propertyValueInIndex;
			this.propertyKeyInIndex = propertyKeyInIndex;
			this.vertices = vertices;
		}

		@Override
		public Iterable<Vertex> get() {
			return vertices.get(propertyKeyInIndex, propertyValueInIndex);
		}

		@Override
		public long size() {
			return vertices.count(propertyKeyInIndex, propertyValueInIndex);
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("IndexLazyLoader [");
			if (propertyKeyInIndex != null) {
				builder.append("key=");
				builder.append(propertyKeyInIndex);
				builder.append(", ");
			}
			if (propertyValueInIndex != null) {
				builder.append("value=");
				builder.append(propertyValueInIndex);
				builder.append(", ");
			}
			builder.append("size()=");
			builder.append(size());
			builder.append("]");
			return builder.toString();
		}
	}

	/**
	 * Result collector. Each of the given vertex set has a count field which
	 * will be used as primary comparison field by built-in comparator
	 */
	private SortedSet<VertexSet> result = new TreeSet<VertexSet>(new VertexSetSizeComparator());

	private AbstractBluePrintsBackedFinderService<?, ?, ?> service;
	/**
	 * Cache of objects being loaded during roots collection building
	 */
	private transient ObjectCache objectsBeingAccessed = ObjectCache.create(CascadeType.REFRESH);

	public VertexRootsCollector(AbstractBluePrintsBackedFinderService<?, ?, ?> service) {
		this.service = service;
	}

	/**
	 * Obtain the sorted set of vertex sets
	 *
	 * @return
	 */
	public SortedSet<VertexSet> getResult() {
		return result;
	}

	/**
	 * Those queries are never visited, as they lead to unresolvable path (or at
	 * least i think so)
	 *
	 * @param orVertexTest
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#startVisit(com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest)
	 */
	@Override
	public boolean startVisit(OrVertexTest orVertexTest) {
		return false;
	}

	/**
	 * Those queries are never visited, as they lead to unresolvable path (or at
	 * least i think so)
	 *
	 * @param notVertexTest
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#startVisit(com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest)
	 */
	@Override
	public boolean startVisit(NotVertexTest notVertexTest) {
		return false;
	}

	/**
	 * @param collectionContains
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#visit(com.dooapp.gaedo.blueprints.queries.tests.CollectionContains)
	 */
	@Override
	public void visit(CollectionContains collectionContains) {
		result.add(load(collectionContains.getExpectedAsValue(), collectionContains.getPath()));
	}

	/**
	 * We use equalsTo test as base for query only when tested value is non null.
	 * @param equalsTo
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#visit(com.dooapp.gaedo.blueprints.queries.tests.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		if(equalsTo.getExpectedAsValue()!=null)
			result.add(load(equalsTo.getExpectedAsValue(), equalsTo.getPath()));
	}

	/**
	 * Load the VertexSet corresponding to the vertex associated with expected
	 * value, and accessible through given path
	 *
	 * @param expected
	 * @param path
	 * @return
	 */
	private <Type> VertexSet load(Type expected, Iterable<Property> path) {
		LinkedList<Property> updatablePath = new LinkedList<Property>(CollectionUtils.asList(path));
		Property lastProperty = updatablePath.removeLast();
		// if value is a literal, well, it's time for an index lookup
		Class<Type> expectedClass = (Class<Type>) expected.getClass();
		if (expected != null && Literals.containsKey(expectedClass)) {
			// Yup : gladly using implementation as it provides a last() method !
			Index<Vertex> vertices = (Index<Vertex>) service.getDatabase().getIndex(IndexNames.VERTICES.getIndexName(), IndexNames.VERTICES.getIndexed());
			// stinky code fragment for collections : as each value is stored under
			// a key in the form propertyName:index, we have to iterate upon them
			String propertyKeyInIndex = null;
			String propertyValueInIndex = null;
			if (Collection.class.isAssignableFrom(lastProperty.getType())) {
				propertyKeyInIndex = GraphUtils.getEdgeNameFor(new LiteralInCollectionUpdaterProperty(lastProperty, expected, Updater.ELEMENT_IN_COLLECTION_MARKER));
				propertyValueInIndex = Updater.ELEMENT_IN_COLLECTION_MARKER_GRAPH_VALUE;
			} else {
				propertyKeyInIndex = GraphUtils.getEdgeNameFor(lastProperty);
				LiteralTransformer<Type> transformer = Literals.get(expectedClass);
				if(Literals.containsKey(lastProperty.getType())) {
					transformer = Literals.get(lastProperty.getType());
				}
				propertyValueInIndex = transformer.toString(expected);
			}
			VertexSet returned = new VertexSet().withPropertyPath(updatablePath);
			returned.setVertices(new IndexLazyLoader(vertices, propertyKeyInIndex, propertyValueInIndex));
			return returned;
		} else {
			Vertex vertexFor = service.getVertexFor(expected, CascadeType.REFRESH, objectsBeingAccessed);
			if (vertexFor == null) {
				return new VertexSet().withVertices(new LinkedList<Vertex>()).withPropertyPath(new LinkedList<Property>());
			} else {
				return new VertexSet().withVertices(Arrays.asList(vertexFor)).withPropertyPath(path);
			}
		}
	}
}
