package com.dooapp.gaedo.blueprints;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;

/**
 * A cache implementation, that should allow less memory consumption of gaedo
 * @author ndx
 *
 */
public class ObjectCache {
	private Map<String, Object> cache = new TreeMap<String, Object>();

	/**
	 * Create objet cache for the given cascade type
	 * TODO optimize heavily by using threadlocal and a map of cascade types to obejct caches (or a better cache implementation)
	 * @param merge
	 * @return
	 */
	public static ObjectCache create(CascadeType merge) {
		return new ObjectCache();
	}

	public Object get(String objectVertexId) {
		return cache.get(objectVertexId);
	}

	public void put(String objectVertexId, Object toUpdate) {
		cache.put(objectVertexId, toUpdate);
	}

	public void remove(String objectVertexId) {
		cache.remove(objectVertexId);
	}

	public boolean containsKey(String objectVertexId) {
		return cache.containsKey(objectVertexId);
	}

}
