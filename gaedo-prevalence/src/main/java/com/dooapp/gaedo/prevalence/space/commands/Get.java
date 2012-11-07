package com.dooapp.gaedo.prevalence.space.commands;

import java.io.Serializable;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

/**
 * Get value associated to given key
 * @author Nicolas
 *
 * @param <Key> key paremeter type
 */
public class Get<Key extends Serializable> implements Command<Object, Key> {
	/**
	 * Searched key
	 */
	private Key key;

	public Get(Key key) {
		this.key = key;
	}

	@Override
	public Object execute(StorageSpace<Key> storage) {
		return storage.get(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Get other = (Get) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
