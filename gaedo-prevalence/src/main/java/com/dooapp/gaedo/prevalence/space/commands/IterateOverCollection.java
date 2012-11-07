package com.dooapp.gaedo.prevalence.space.commands;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class IterateOverCollection<DataType, Key extends Serializable>
		implements Command<Iterable<DataType>, Key>, Serializable {

	private Key key;

	public IterateOverCollection(Key storageName) {
		this.key = storageName;
	}

	/**
	 * Create a read-only copy of content to iterate over it (in order to avoid the classical bug of the ConcurrentModificationException)
	 */
	@Override
	public Iterable<DataType> execute(StorageSpace<Key> storage) {
		return Collections.unmodifiableCollection(new LinkedList<DataType>((Collection<DataType>) storage.get(key)));
	}

}
