package com.dooapp.gaedo.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * Facade for property provider providing a caching feature (hope this will accelerate code)
 * @author ndx
 *
 */
public class CachingPropertyProvider implements PropertyProvider {
	/**
	 * Cache of loaded classes to properties association
	 */
	private Map<Class<?>, Property[]> classes = new HashMap<Class<?>, Property[]>();
	/**
	 * Source property provider
	 */
	private final PropertyProvider source;

	public CachingPropertyProvider(PropertyProvider source) {
		super();
		this.source = source;
	}

	@Override
	public Property[] get(Class<?> containedClass) {
		if(!classes.containsKey(containedClass)) {
			classes.put(containedClass, source.get(containedClass));
		}
		return classes.get(containedClass);
	}

}
