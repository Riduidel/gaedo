package com.dooapp.gaedo.google.datastore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.SortingExpression;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Get elements from datastore by query lookup, associated with sorting mode (which is never used in that case)
 * @author ndx
 *
 */
public class KeysQueryLookup implements DataStoreExecutableQuery {
	private Map<Key, Entity> result;

	/**
	 * Get a bunch of entites from a bunch of keys
	 * @param datastore used datastore
	 * @param keys inpyt keys
	 * @param expectedKind expected result kind. when getting data of sub-kind, we try to go back to expected kind and otherwise send an excpetion
	 */
	public KeysQueryLookup(DatastoreService datastore, Collection<Key> keys, String expectedKind) {
		// Do some key transtyping
		Collection<Key> typedKeys = new LinkedList<Key>();
		for(Key inputKey : keys) {
			Key key = inputKey;
			while(!(expectedKind.equals(key.getKind()) && key!=null)) {
				key = key.getParent();
			}
			if(key==null) {
				throw new IncompatibleKeyException(inputKey, expectedKind);
			}
			typedKeys.add(key);
		}
		result = datastore.get(typedKeys);
	}

	@Override
	public int count() {
		return result.size();
	}

	@Override
	public Iterable<Entity> getAll(int start, int end) {
		Collection<Entity> returned = new LinkedList<Entity>();
		int index=0;
		for(Entity e: result.values()) {
			if(index>=start && index<end) {
				returned.add(e);
			}
		}
		return returned;
	}

	@Override
	public Iterable<Entity> getAll() {
		return result.values();
	}

	@Override
	public Entity getEntity() {
		return result.values().iterator().next();
	}

}
