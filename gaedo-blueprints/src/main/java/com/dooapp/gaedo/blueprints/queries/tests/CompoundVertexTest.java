package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Collection;
import java.util.regex.Pattern;

import com.dooapp.gaedo.properties.Property;

/**
 * Interface implemented by queries considered as compositions of other ones (AND, OR, ...)
 * @author ndx
 *
 */
public interface CompoundVertexTest extends VertexTest {

	/**
	 * Creates a vertex test for equality checking, AND (as a side effect) add test to current list (making the {@link #add(VertexTest)} call useless)
	 * @param field
	 * @param value
	 * @return
	 */
	VertexTest equalsTo(Iterable<Property> path, Object value);

	public <Type extends VertexTest> Type add(Type test);

	CompoundVertexTest or();

	CompoundVertexTest and();

	CompoundVertexTest not();

	/**
	 * Creates a vertex test for greater than checking, AND (as a side effect) add test to current list (making the {@link #add(VertexTest)} call useless)
	 * @param value
	 * @param strictly TODO
	 * @param field
	 * @return
	 */
	<ComparableType extends Comparable<ComparableType>> VertexTest greaterThan(Iterable<Property> path, ComparableType value, boolean strictly);

	/**
	 * Creates a vertex test for lower than checking, AND (as a side effect) add test to current list (making the {@link #add(VertexTest)} call useless)
	 * @param value
	 * @param strictly TODO
	 * @param field
	 * @return
	 */
	<ComparableType extends Comparable<ComparableType>> VertexTest lowerThan(Iterable<Property> path, ComparableType value, boolean strictly);

	/**
	 * Creates a vertex test for contains string checking, AND (as a side effect) add test to current list (making the {@link #add(VertexTest)} call useless)
	 * @param field
	 * @param value
	 * @return
	 */
	VertexTest containsString(Iterable<Property> path, String contained);

	VertexTest startsWith(Iterable<Property> path, String start);

	VertexTest endsWith(Iterable<Property> path, String end);

	VertexTest matches(Iterable<Property> path, Pattern pattern);

	VertexTest collectionContains(Iterable<Property> path, Object contained);

	VertexTest mapContainsValue(Iterable<Property> path, Object contained);

	VertexTest mapContainsKey(Iterable<Property> path, Object contained);

	VertexTest anything(Iterable<Property> path);

	VertexTest equalsToIgnoreCase(Iterable<Property> path, String compared);

	VertexTest instanceOf(Iterable<Property> path, Class<?> type);

	/**
	 * Get contained list of tests. Please do not call it from user space, as it is of virtually no use to you.
	 * @return
	 */
	Collection<VertexTest> getTests();
}
