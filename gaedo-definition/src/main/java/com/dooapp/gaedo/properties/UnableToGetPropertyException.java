package com.dooapp.gaedo.properties;

/**
 * Sent when the get method do not works
 * @author ndx
 *
 */
public class UnableToGetPropertyException extends PropertyProviderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnableToGetPropertyException(Property propertyField, Exception e) {
		super("unable to perform a get on field "+propertyField.toGenericString(), e);
	}
	
}