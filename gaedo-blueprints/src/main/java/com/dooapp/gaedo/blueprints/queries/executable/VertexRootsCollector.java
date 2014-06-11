package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexNames;
import com.dooapp.gaedo.blueprints.operations.CollectionAccessByValueProperty;
import com.dooapp.gaedo.blueprints.operations.Updater;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.queries.tests.EqualsTo;
import com.dooapp.gaedo.blueprints.queries.tests.InstanceOf;
import com.dooapp.gaedo.blueprints.queries.tests.NotVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.OrVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.blueprints.queries.tests.VertextTestUtils;
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
	private final class IndexLazyLoader implements LazyLoader, Comparable<LazyLoader> {
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

		@Override
		public LazyLoader getSourceLoader() {
			return this;
		}

		@Override
		public int compareTo(LazyLoader o) {
			if (o instanceof IndexLazyLoader) {
				IndexLazyLoader loader = (IndexLazyLoader) o;
				int returned = 0;
				if(returned==0) {
					returned = propertyKeyInIndex.compareTo(loader.propertyKeyInIndex);
				}
				if(returned==0) {
					returned = propertyValueInIndex.compareTo(loader.propertyValueInIndex);
				}
				return returned;
			} else {
				return getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Temporary map linking sets to the tests that should be executed, were those sets to be chosen as reference ones
	 *
	 */
	private Map<VertexSet, VertexTest> result = new LinkedHashMap<VertexSet, VertexTest>();

	private final AbstractBluePrintsBackedFinderService<?, ?, ?> service;
	/**
	 * Cache of objects being loaded during roots collection building
	 */
	private transient ObjectCache objectsBeingAccessed = ObjectCache.create(CascadeType.REFRESH);

	private VertexTest initialTest;

	public VertexRootsCollector(AbstractBluePrintsBackedFinderService<?, ?, ?> service) {
		this.service = service;
	}

	/**
	 * Obtain the sorted set of vertex sets
	 *
	 * @return
	 */
	public Map<VertexSet, VertexTest> getSetsToProcessedTests(VertexTest testToScan) {
		this.initialTest = testToScan;
		testToScan.accept(this);
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
		result.put(load(collectionContains.getExpectedAsValue(), collectionContains.getPath()), testWithout(collectionContains));
	}

	@Override
	public void visit(InstanceOf instanceOf) {
		if(instanceOf.getRepository().containsKey(instanceOf.getExpectedAsValue())) {
			result.put(load(instanceOf.getExpectedAsValue(), instanceOf.getPath()), testWithout(instanceOf));
		}
	}

	/**
	 * We use equalsTo test as base for query only when tested value is non null.
	 * @param equalsTo
	 * @see com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter#visit(com.dooapp.gaedo.blueprints.queries.tests.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		if(equalsTo.getExpectedAsValue()!=null)
			result.put(load(equalsTo.getExpectedAsValue(), equalsTo.getPath()), testWithout(equalsTo));
	}

	/**
	 * Create a test from initial one without the given one
	 * @param toRemove test to remove from initial test
	 * @return
	 */
	private VertexTest testWithout(VertexTest toRemove) {
		return VertextTestUtils.testWithout(initialTest, toRemove);
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
				propertyKeyInIndex = GraphUtils.getEdgeNameFor(new CollectionAccessByValueProperty(lastProperty, expected, Updater.ELEMENT_IN_COLLECTION_MARKER));
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
				return new VertexSet().withVertices(Arrays.asList(vertexFor)).withPropertyPath(new LinkedList<Property>(CollectionUtils.asList(path)));
			}
		}
	}
}
