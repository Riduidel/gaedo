package com.dooapp.gaedo.blueprints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import javax.persistence.CascadeType;

/**
 * A cache implementation, that should allow less memory consumption of gaedo
 * @author ndx
 *
 */
public class ObjectCache {
	private Map<String, Reference<Object>> cache = new WeakHashMap<String, Reference<Object>>();

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
		Reference<Object> reference = cache.get(objectVertexId);
		if(reference!=null) {
			Object returned = reference.get();
			if(returned==null) {
				cache.remove(objectVertexId);
			}
			return returned;
		} else {
			return null;
		}
	}

	public void put(String objectVertexId, Object toUpdate) {
		cache.put(objectVertexId, new WeakReference<Object>(toUpdate));
	}

	public void remove(String objectVertexId) {
		cache.remove(objectVertexId);
	}
}
