package com.dooapp.gaedo.utils;

import java.util.Arrays;
import java.util.Collection;

/**
 * Exception thrown when creation of invocation handler failed
 * @author ndx
 *
 */
public class UnableToCreateInvocationHandlerException extends
		VirtualMethodCreationException {

	public UnableToCreateInvocationHandlerException(
			Class<?>[] toImplement, Collection<VirtualMethodCreationException> exceptions) {
		super("Unable to create invocation handler for "+Arrays.toString(toImplement)+"\n\tdue to exceptions :\n"+ExceptionUtils.collectMessages(exceptions));
	}

}
