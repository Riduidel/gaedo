package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.id.BadIdAnnotatedClassException;


public class NoSuchIdManagerException extends BadIdAnnotatedClassException {

	public NoSuchIdManagerException(Field f) {
		super("there is no id manager available for field "+f);
	}

}
