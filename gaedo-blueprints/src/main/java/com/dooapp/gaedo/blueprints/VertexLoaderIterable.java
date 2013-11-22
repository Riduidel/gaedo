package com.dooapp.gaedo.blueprints;

import java.util.Iterator;

import com.tinkerpop.blueprints.Vertex;

public class VertexLoaderIterable<DataType> implements Iterable<DataType> {
	private class VertexLoaderIterator implements Iterator<DataType> {

		private Iterator<Vertex> iterator;

		public VertexLoaderIterator(Iterator<Vertex> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public DataType next() {
			return service.loadObject(iterator.next(), cache);
		}

		@Override
		public void remove() {
			iterator.remove();
		}

	}

	private AbstractBluePrintsBackedFinderService<?, DataType, ?> service;
	private Iterable<Vertex> vertices;
	private ObjectCache cache = new ObjectCache();

	public VertexLoaderIterable(AbstractBluePrintsBackedFinderService<?, DataType, ?> abstractBluePrintsBackedFinderService,
					Iterable<Vertex> verticesIterable) {
		this.service = abstractBluePrintsBackedFinderService;
		this.vertices = verticesIterable;
	}

	@Override
	public Iterator<DataType> iterator() {
		return new VertexLoaderIterator(vertices.iterator());
	}

}
