package com.dooapp.gaedo.google.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * Embedds a query and associated prepared query to run on datastore
 * @author ndx
 *
 */
public class ClassicalQuery implements DataStoreExecutableQuery {
	/**
	 * PreparedQuery that will be executed
	 */
	private PreparedQuery query;

	public ClassicalQuery(DatastoreService datastore, Query query) {
		this.query = datastore.prepare(query);
	}

	@Override
	public int count() {
		return query.countEntities();
	}

	@Override
	public Iterable<Entity> getAll(int start, int end) {
		FetchOptions fetchOptions = FetchOptions.Builder.withOffset(start).limit(end);
		return query.asIterable(fetchOptions);
	}

	@Override
	public Iterable<Entity> getAll() {
		return query.asIterable();
	}

	@Override
	public Entity getEntity() {
		return query.asSingleEntity();
	}

}
