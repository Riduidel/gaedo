package com.dooapp.gaedo.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some utility methods regarding collections
 * @author ndx
 *
 */
public class CollectionUtils {

	/**
	 * Build map from an array
	 * @param parameters
	 * @return
	 */
	public static Map<String, Object> asMap(Object... parameters) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// Ensure index ends correctly
		int max = (parameters.length/2)*2;
		for (int i = 0; i < max; i++) {
			paramMap.put(parameters[i++].toString(), parameters[i]);
		}
		return paramMap;
	}

	/**
	 * Convert any object into a list
	 * @param source
	 * @return
	 */
	public static List asList(Object source) {
		if (source instanceof Iterable) {
			Iterable new_name = (Iterable) source;
			return asList(new_name);
		} else {
			return asList(Arrays.asList(source));
		}
	}

	/**
	 * Build a list of bean from the provided iterable
	 * @param source any kind of iterable
	 * @return a {@link Serializable} {@link List} containing the same element set
	 */
	public static <Bean> List<Bean> asList(Iterable<Bean> source) {
		if(source instanceof List && source instanceof Serializable) {
			return (List<Bean>) source;
		} else if(source==null) {
			return Collections.emptyList();
		} else {
			List<Bean> returned = new LinkedList<Bean>();
			for(Bean b : source) {
				returned.add(b);
			}
			return returned;
		}
	}

	/**
	 * Build a set of bean from the provided iterable
	 * @param source any kind of iterable
	 * @return a {@link Serializable} {@link Set} containing the same element set
	 */
	public static <Bean> Set<Bean> asSet(Iterable<Bean> source) {
		if(source instanceof Set && source instanceof Serializable) {
			return (Set<Bean>) source;
		} else if(source==null) {
			return Collections.emptySet();
		} else {
			Set<Bean> returned = new HashSet<Bean>();
			for(Bean b : source) {
				returned.add(b);
			}
			return returned;
		}
	}

}
