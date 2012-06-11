package com.dooapp.gaedo.finders.dynamic;

import java.lang.reflect.Method;

/**
 * Event sent when a method is created
 * @author Nicolas
 *
 */
public class MethodCreatedEvent {

	private final Method method;
	private final DynamicFinderMethodResolver resolver;

	public MethodCreatedEvent(Method method, DynamicFinderMethodResolver resolver) {
		this.method = method;
		this.resolver = resolver;
	}

	public Method getMethod() {
		return method;
	}

	public DynamicFinderMethodResolver getResolver() {
		return resolver;
	}

}
