package com.dooapp.gaedo.rest.server;

class UnrepresentableObjectException extends RestServerException {

	public UnrepresentableObjectException(Object source) {
		super("No representation can be made out of "+source+" of class "+source.getClass().getName());
	}
	
}