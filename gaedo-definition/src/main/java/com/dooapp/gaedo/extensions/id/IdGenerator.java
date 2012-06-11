package com.dooapp.gaedo.extensions.id;

public interface IdGenerator<DataType> {
	public void generateIdFor(DataType value);
}
