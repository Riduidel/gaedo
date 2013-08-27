package com.dooapp.gaedo.finders;

import com.dooapp.gaedo.exceptions.BadRangeException;

/**
 * Interface defining methods allowing one to browse some query results
 * @author ndx
 *
 * @param <ValueType>
 */
public interface QueryBrowser<ValueType> {
	/**
	 * Get first available data
	 * @return
	 */
	ValueType getFirst();
	/**
	 * Iterate through all available data
	 * @return
	 */
	Iterable<ValueType> getAll();
	/**
	 * Iterate through a subset of all results
	 * @param start start index
	 * @param end end index
	 * @return an iterable over the results
	 * @throws may throws a {@link BadRangeException}
	 */
	Iterable<ValueType> get(int start, int end);
	/**
	 * Get data count
	 * @return
	 */
	int count();

}
