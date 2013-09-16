package com.dooapp.gaedo.blueprints.queries;

import java.io.ObjectStreamException;
import java.util.Iterator;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphQueryStatement.ProjectionExecutor;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

/**
 * That Iterable iterates over a list of (qsorted) vertices and load objects during that iteration.
 * In other wiords, it means an object doesn't exist as long as its associated vertex hasn't been iterated over.
 * @author ndx
 *
 * @param <DataType>
 */
public class DataTypeIterable<DataType> implements Iterable<DataType>, WriteReplaceable {
	/**
	 * Inner iterator navigating the list of vertices and loading associated objects.
	 * @author ndx
	 *
	 */
	private class DataTypeIterator implements Iterator<DataType> {
		private Iterator<Vertex> verticesIterator = vertices.iterator();


		/**
		 * @return
		 * @see java.util.Iterator#hasNext()
		 * @category delegate
		 */
		public boolean hasNext() {
			return verticesIterator.hasNext();
		}

		/**
		 * @return
		 * @see java.util.Iterator#next()
		 * @category delegate
		 */
		@SuppressWarnings("unchecked")
		public DataType next() {
			// due to the fact that projection executor is an inner class, some of the typing info is lost
			return (DataType) projectionExecutor.get(verticesIterator.next());
		}

		/**
		 *
		 * @see java.util.Iterator#remove()
		 * @category delegate
		 */
		public void remove() {
			verticesIterator.remove();
		}

	}
	/**
	 * Sorted list of vertices from which objects are to be loaded.
	 */
	private Iterable<Vertex> vertices;
	private ProjectionExecutor projectionExecutor;

	/**
	 * Construct the iterable by giving it both the service and the list of vertices.
	 * @param service service used to laod vertices
	 * @param asIterable vertices to navigate. That vertices list MUST be ordered before being given to this object.
	 * @param projectionExecutor object allowing transformation from vertices into ValueType objects.
	 */
	public DataTypeIterable(Iterable<Vertex> asIterable, ProjectionExecutor projectionExecutor) {
		this.vertices = asIterable;
		this.projectionExecutor = projectionExecutor;
	}

	@Override
	public DataTypeIterator iterator() {
		return new DataTypeIterator();
	}

	@Override
	public Object writeReplace() throws ObjectStreamException {
		return CollectionUtils.asList(this);
	}

}
