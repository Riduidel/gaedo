package com.dooapp.gaedo.prevalence.space.commands;

import java.io.Serializable;
import java.security.Key;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class Create<Key extends Serializable> implements Command<Void, Key>, Serializable {
	private static class StorageAlreadyContainsKey extends CommandExecutionException {

		public StorageAlreadyContainsKey(Object key) {
			super("storage already contains key "+key.toString());
		}
		
	}
	/**
	 * Element new key
	 */
	private final Key key;
	
	/**
	 * Element new data
	 */
	private final Serializable data;
	
	public Create(Key elementKey, Serializable data) {
		super();
		this.key = elementKey;
		this.data = data;
	}

	@Override
	public Void execute(StorageSpace<Key> storage) {
		if(storage.contains(key))
			throw new StorageAlreadyContainsKey(key);
		storage.put(key, data);
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
		Create other = (Create) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
	
}
