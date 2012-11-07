package com.dooapp.gaedo.properties;

/**
 * Sent when set operation fails
 * @author ndx
 *
 */
public class UnableToSetPropertyException extends PropertyProviderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnableToSetPropertyException(Property propertyField, Exception e) {
		super("unable to perform a set on field "+propertyField.toGenericString(), e);
	}
	
}