package com.dooapp.gaedo.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Some utility methods
 * @author ndx
 *
 */
public class PropertyProviderUtils {
	/**
	 * Make a map from the loaded properties array. insertion order is here fully respected.
	 * @param properties
	 * @return a {@link LinkedHashMap} of {@link Property#getName()} to properties
	 */
	public static Map<String, Property> asMap(Property[] properties) {
		Map<String, Property> returned = new LinkedHashMap<String, Property>();
		for(Property p : properties) {
			returned.put(p.getName(), p);
		}
		return returned ;
	}

	/**
	 * Get all properties of the given class, be there inherited or in-class ones
	 * @param provider
	 * @param containedClass
	 * @return
	 */
	public static Property[] getAllProperties(PropertyProvider provider, Class<?> containedClass) {
		Collection<Property> temporary = new LinkedList<Property>();
		Class<?> currentClass = containedClass;
		while(!Object.class.equals(currentClass)) {
			temporary.addAll(Arrays.asList(provider.get(currentClass)));
			currentClass = currentClass.getSuperclass();
		}
		return temporary.toArray(new Property[temporary.size()]);
	}
}
