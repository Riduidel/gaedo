package com.dooapp.gaedo.prevalence.space.commands;

import java.io.Serializable;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class Contains<Key extends Serializable> implements Command<Boolean, Key> {

	private Key key;

	public Contains(Key key) {
		super();
		this.key = key;
	}

	@Override
	public Boolean execute(StorageSpace<Key> storage) {
		return storage.contains(key);
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
		Contains other = (Contains) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
