package com.dooapp.gaedo.finders.root;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.properties.Property;

public class NoLocatorAllowsFieldException extends CrudServiceException {

	public NoLocatorAllowsFieldException(Property field,
			Map<FieldInformerLocator, Exception> thrownDuringSearch) {
		super("field "+field.getDeclaringClass().getName()+"#"+field.getName()+" cannot be informed using any of the available locators :"+getExceptionsAsString(thrownDuringSearch));
	}

	public NoLocatorAllowsFieldException(Class informedClass, String fieldName, Map<FieldInformerLocator, Exception> thrownDuringSearch) {
		super("field "+informedClass.getName()+"#"+fieldName+" cannot be informed using any of the available locators :"+getExceptionsAsString(thrownDuringSearch));
	}

	private static String getExceptionsAsString(
			Map<FieldInformerLocator, Exception> thrownDuringSearch) {
		StringBuilder sOut = new StringBuilder();
		for(Map.Entry<FieldInformerLocator, Exception> mapException : thrownDuringSearch.entrySet()) {
			sOut.append("\n").append(mapException.getKey().getClass().getName()).append(" => ")
				.append(mapException.getValue().getMessage());
		}
		return sOut.toString();
	}

	private static String getStack(Exception value) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(stream);
		value.printStackTrace(print);
		return stream.toString();
	}

}
