package com.dooapp.gaedo.blueprints.queries;

import java.io.ObjectStreamException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
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
		 * Cache-like map allowing a little faster iteration mechanism
		 */
		private Map<String, Object> objectsBeingAccessed = new TreeMap<String, Object>();

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
		public DataType next() {
			return service.loadObject(verticesIterator.next(), objectsBeingAccessed);
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
	 * Service used to load the objects
	 */
	private AbstractBluePrintsBackedFinderService<?, DataType, ?> service;
	/**
	 * Sorted list of vertices from which objects are to be loaded.
	 */
	private Iterable<Vertex> vertices;

	/**
	 * Construct the iterable by giving it both the service and the list of vertices.
	 * @param service service used to laod vertices
	 * @param asIterable vertices to navigate. That vertices list MUST be ordered before being given to this object.
	 */
	public DataTypeIterable(AbstractBluePrintsBackedFinderService<?, DataType, ?> service, Iterable<Vertex> asIterable) {
		this.service = service;
		this.vertices = asIterable;
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
