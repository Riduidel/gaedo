package com.dooapp.gaedo.prevalence.space.basic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class SimpleStorageSpace<Key extends Serializable> implements StorageSpace<Key>{
	/**
	 * Effective storage map
	 */
	private Map<Key, Object> storageMap;
	
	public SimpleStorageSpace() {
		this(new HashMap<Key, Object>());
	}

	public SimpleStorageSpace(Map<Key, Object> storageMap) {
		this.storageMap = storageMap;
	}

	@Override
	public boolean contains(Key key) {
		return storageMap.containsKey(key);
	}

	@Override
	public Object get(Key key) {
		return storageMap.get(key);
	}

	@Override
	public Object put(Key key, Object obj) {
		return storageMap.put(key, obj);
	}

	@Override
	public Object remove(Key key) {
		return storageMap.remove(key);
	}
}
