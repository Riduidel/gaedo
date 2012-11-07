package com.dooapp.gaedo.blueprints;

public class UnspecifiedClassLoader extends BluePrintsCrudServiceException {
	public UnspecifiedClassLoader() {
		super("invoking this method without a classloader set will undoubtly result into a classloader clash");
	}
}
