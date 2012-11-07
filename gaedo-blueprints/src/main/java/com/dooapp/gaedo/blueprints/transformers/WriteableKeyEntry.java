package com.dooapp.gaedo.blueprints.transformers;

import java.util.Map;
import java.util.Map.Entry;

/**
 * An impelmentation of {@link Entry} allowing us to write key, very useful to load objects !
 * @author ndx
 *
 * @param <K>
 * @param <V>
 */
public class WriteableKeyEntry<K, V> implements Map.Entry<K, V> {
	private K key;
	private V value;
	
	public WriteableKeyEntry() {
		
	}

	public WriteableKeyEntry(K key, V value) {
		setKey(key);
		setValue(value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}
	
	public K setKey(K key) {
		K old = this.key;
		this.key = key;
		return old;
	}
}