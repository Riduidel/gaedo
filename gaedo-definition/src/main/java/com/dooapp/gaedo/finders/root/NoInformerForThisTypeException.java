package com.dooapp.gaedo.finders.root;


public class NoInformerForThisTypeException extends UnableToLocateInformerForException {

	public NoInformerForThisTypeException(Class<?> clazz) {
		super("unable to find a FieldInformer class for a field of class "+clazz.getName()+". Please contact gaedo team to let them write one");
	}

}
