package com.dooapp.gaedo.prevalence.space.commands;

import java.io.Serializable;
import java.util.Collection;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class PutInCollection<Key extends Serializable> implements Command<Boolean, Key>, Serializable {
	
	private Key key;
	private Serializable value;

	public PutInCollection(Key key, Serializable value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public Boolean execute(StorageSpace<Key> storage) {
		// Object associated to key must be a collection.
		// This is not checked, to ensure fail-fast of this layer
		Collection content = (Collection) storage.get(key);
		return Boolean.valueOf(content.add(value));
	}

}
