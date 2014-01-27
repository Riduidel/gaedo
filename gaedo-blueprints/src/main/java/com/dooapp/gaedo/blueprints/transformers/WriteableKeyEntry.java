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

	/**
	 * @param value new value for #value
	 * @category fluent
	 * @category setter
	 * @category value
	 * @return this object for chaining calls
	 */
	public WriteableKeyEntry<K, V> withValue(V value) {
		this.setValue(value);
		return this;
	}

	public K setKey(K key) {
		K old = this.key;
		this.key = key;
		return old;
	}

	/**
	 * @param key new value for #key
	 * @category fluent
	 * @category setter
	 * @category key
	 * @return this object for chaining calls
	 */
	public WriteableKeyEntry<K, V> withKey(K key) {
		this.setKey(key);
		return this;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WriteableKeyEntry [");
		if (key != null) {
			builder.append("key=");
			builder.append(key);
			builder.append(", ");
		}
		if (value != null) {
			builder.append("value=");
			builder.append(value);
		}
		builder.append("]");
		return builder.toString();
	}
}