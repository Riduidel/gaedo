package com.dooapp.gaedo.prevalence.space.commands;

import java.io.Serializable;
import java.util.Collection;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class UpdateInCollection<DataType extends Serializable, Key extends Serializable> implements Command<DataType, Key>, Serializable {

	private Key key;
	private DataType value;

	public UpdateInCollection(Key storageName, DataType toUpdate) {
		this.key = storageName;
		this.value = toUpdate;
	}

	@Override
	public DataType execute(StorageSpace<Key> storage) {
		// Object associated to key must be a collection.
		// This is not checked, to ensure fail-fast of this layer
		Collection content = (Collection) storage.get(key);
		content.remove(value);
		content.add(value);
		return value;
	}

}
