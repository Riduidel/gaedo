package com.dooapp.gaedo.blueprints.queries;

import java.io.ObjectStreamException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.pgm.Vertex;

public class DataTypeIterable<DataType> implements Iterable<DataType>, WriteReplaceable {
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

	private AbstractBluePrintsBackedFinderService<?, DataType, ?> service;
	private Iterable<Vertex> vertices;

	public DataTypeIterable(AbstractBluePrintsBackedFinderService<?, DataType, ?> service, Iterable<Vertex> asIterable) {
		this.service = service;
		this.vertices = asIterable;
	}

	@Override
	public Iterator<DataType> iterator() {
		return new DataTypeIterator();
	}

	@Override
	public Object writeReplace() throws ObjectStreamException {
		return CollectionUtils.asList(this);
	}

}
