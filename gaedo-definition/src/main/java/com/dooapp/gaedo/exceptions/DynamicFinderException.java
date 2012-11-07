package com.dooapp.gaedo.exceptions;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.dynamic.DynamicFinder;
import com.dooapp.gaedo.finders.dynamic.DynamicFinderMethodResolver;
import com.dooapp.gaedo.utils.VirtualMethodCreationException;

/**
 * Base for all exceptions thrown while constructing the {@link DynamicFinderMethodResolver} associated to a method declared in a subinterface of {@link DynamicFinder}
 * @author ndx
 *
 */
public class DynamicFinderException extends VirtualMethodCreationException {

	public DynamicFinderException() {
	}

	public DynamicFinderException(String message) {
		super(message);
	}

	public DynamicFinderException(Throwable cause) {
		super(cause);
	}

	public DynamicFinderException(String message, Throwable cause) {
		super(message, cause);
	}

}
