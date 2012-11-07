package com.dooapp.gaedo.google.datastore;

import java.util.Iterator;

import com.google.appengine.api.datastore.Entity;

/**
 * This Iterable allows to browse the data results
 * @author ndx
 *
 */
public class DataTypeIterable<DataType> implements Iterable<DataType> {
	private class DataTypeIterator implements Iterator<DataType> {

		private Iterator<Entity> entityIterator;

		public DataTypeIterator(Iterator<Entity> iterator) {
			this.entityIterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return entityIterator.hasNext();
		}

		@Override
		public DataType next() {
			return service.getObject(entityIterator.next());
		}

		@Override
		public void remove() {
			entityIterator.remove();
		}
		
	}

	/**
	 * Used service
	 */
	private final DatastoreFinderService<DataType, ?> service;
	/**
	 * Iterator over datastore entities
	 */
	private Iterable<Entity> entityIterable;

	/**
	 * Build the data type iterable object
	 * @param service service used to re-hydrate objects
	 * @param asIterable iterable object used to browse the data result
	 */
	public DataTypeIterable(DatastoreFinderService<DataType, ?> service, Iterable<Entity> asIterable) {
		this.service = service;
		this.entityIterable = asIterable;
	}

	@Override
	public Iterator<DataType> iterator() {
		return new DataTypeIterator(entityIterable.iterator());
	}
	
}