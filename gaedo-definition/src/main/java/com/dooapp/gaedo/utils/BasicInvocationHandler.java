package com.dooapp.gaedo.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dooapp.gaedo.CrudServiceException;

/**
 * Base class for some of our invocation handlers. This class provides some
 * useful method for handling comon cases
 * 
 * @author ndx
 * 
 */
public abstract class BasicInvocationHandler<ImplementedType, VirtualMethodResolver extends MethodResolver> {

	/**
	 * Map of method resolvers.
	 */
	private final Map<Method, VirtualMethodResolver> resolvers = new HashMap<Method, VirtualMethodResolver>();

	/**
	 * Map of method resolvers usable by subclass. This one is a read-only view
	 * of {@link #resolvers}.
	 */
	protected final Map<Method, VirtualMethodResolver> loadedResolvers = Collections.unmodifiableMap(resolvers);

	/**
	 * Target interface implemented here
	 */
	protected final Class<ImplementedType> toImplement;

	public BasicInvocationHandler(Class<ImplementedType> toImplement) {
		super();
		this.toImplement = toImplement;
	}

	/**
	 * Create method resolvers for the given class (which *must* denote an
	 * interface and all its super interfaces. This allows simpler code to be
	 * written in later invoke handlers
	 * 
	 * @param toImplement
	 */
	protected void createAllMethodResolvers(Class<?> toImplement) {
		createResolversForDeclaredMethods(toImplement);
		for (Class<?> parentInterface : toImplement.getInterfaces()) {
			createAllMethodResolvers(parentInterface);
		}
	}

	/**
	 * Create all method resolvers. For that, the declared methods of the class
	 * to implement are all browsed, and a resolver is created for each.<br/>
	 * Notice it is a good practice to invoke this method during construction
	 * 
	 * @param toImplement
	 *            target class. Each method of this class will have a resolver
	 *            associated to it
	 */
	protected void createResolversForDeclaredMethods(Class<?>... toImplement) {
		List<VirtualMethodCreationException> exceptions = new LinkedList<VirtualMethodCreationException>();
		for (Class<?> currentClassToImplement : toImplement) {
			for (Method m : currentClassToImplement.getDeclaredMethods()) {
				try {
					getResolver(m);
				} catch (VirtualMethodCreationException e) {
					exceptions.add(e);
				}
			}
		}
		if (exceptions.size() > 0) {
			// in case of a simple exception, we try to be quite informative one
			// xception root cause
			if (exceptions.size() == 1) {
				throw exceptions.get(0);
			}
			// when there are multiple ones, we will only give a shortened
			// message, however complete
			throw new UnableToCreateInvocationHandlerException(toImplement, exceptions);
		}
	}

	/**
	 * Get resolver and cache it
	 * 
	 * @param method
	 * @return
	 */
	protected VirtualMethodResolver getResolver(Method method) {
		if (!resolvers.containsKey(method)) {
			resolvers.put(method, createResolver(method));
		}
		return resolvers.get(method);
	}

	/**
	 * Create method resolver for the given method
	 * 
	 * @param method
	 * @return
	 * @throws VirtualMethodCreationException
	 *             TODO
	 */
	protected abstract VirtualMethodResolver createResolver(Method method) throws VirtualMethodCreationException;

	/**
	 * @param proxy
	 *            proxy object, considered as useless
	 * @param invokedMethod
	 *            method object corresponding to the invoked code.
	 * @param invokedArgs
	 *            invocation arguments.
	 */
	public Object invoke(Object proxy, Method invokedMethod, Object[] invokedArgs) throws Throwable {
		MethodResolver resolver = getResolver(invokedMethod);
		try {
			return resolver.call(invokedArgs);
		} catch(InvocationTargetException e) {
			if(e.getCause() instanceof CrudServiceException) {
				throw ((CrudServiceException)e.getCause());
			} else {
				throw e;
			}
		}
	}

}
