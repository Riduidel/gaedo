package com.dooapp.gaedo.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	public static class ComparatorUsingCompare<Bean extends Comparable<? super Bean>> implements Comparator<Bean> {
		@Override
		public int compare(Bean o1, Bean o2) {
			return o1.compareTo(o2);
		}
	}

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

	public static <Bean extends Comparable<? super Bean>> int compare(Collection<Bean> firstCollection, Collection<Bean> secondCollection) {
		return compare(firstCollection, secondCollection, new ComparatorUsingCompare<Bean>());
	}

	public static <Bean> int compare(Collection<Bean> firstCollection, Collection<Bean> secondCollection,
					Comparator<Bean> comparator) {
		int returned = (int) Math.signum(firstCollection.size()-secondCollection.size());
		if(returned==0)
			returned = compare((Iterable<Bean>)firstCollection, (Iterable<Bean>)secondCollection, comparator);
		return returned;

	}
	public static <Bean extends Comparable<? super Bean>> int compare(Iterable<Bean> firstIterable, Iterable<Bean> secondIterable) {
		return compare(firstIterable, secondIterable, new ComparatorUsingCompare<Bean>());
	}

	public static <Bean> int compare(Iterable<Bean> firstIterable, Iterable<Bean> secondIterable,
					Comparator<Bean> comparator) {
		Iterator<Bean> firstIterator = firstIterable.iterator();
		Iterator<Bean> secondIterator = secondIterable.iterator();
		int returned = 0;
		for(Bean first = null, second = null;
						returned==0 && (firstIterator.hasNext() || secondIterator.hasNext());
						first=null, second=null) {
			boolean firstHasNext = firstIterator.hasNext();
			boolean secondHasNext = secondIterator.hasNext();
			if(firstHasNext) {
				first = firstIterator.next();
			} else {
				returned = secondHasNext ? -1 : 0;
			}
			if(secondHasNext) {
				second = secondIterator.next();
			} else {
				returned = firstHasNext ? 1 : 0;
			}
			if(returned==0) {
				returned = comparator.compare(first, second);
			}
		}
		return returned;
	}

}
