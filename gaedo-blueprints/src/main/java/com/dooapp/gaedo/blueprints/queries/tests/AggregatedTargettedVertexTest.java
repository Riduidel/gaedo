package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Collection;
import java.util.LinkedList;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;

public abstract class AggregatedTargettedVertexTest extends TargettedVertexTest implements CompoundVertexTest{

	protected Collection<VertexTest> tests = new LinkedList<VertexTest>();

	public AggregatedTargettedVertexTest(ServiceRepository repository, Iterable<Property> p) {
		super(repository, p);
	}

	public <Type extends VertexTest> Type add(Type test) {
		this.tests.add(test);
		return test;
	}
	
	public OrVertexTest or() {
		return add(new OrVertexTest(repository, path));
	}
	
	public AndVertexTest and() {
		return add(new AndVertexTest(repository, path));
	}
	
	public NotVertexTest not() {
		return add(new NotVertexTest(repository, path));
	}

	/**
	 * Adds a new {@link EqualsTo} test to {@link #tests} and returns it
	 * @param path
	 * @param value
	 * @see com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest#equalsTo(com.dooapp.gaedo.properties.Property, java.lang.Object)
	 * @category simple
	 */
	public VertexTest equalsTo(Iterable<Property> path, Object value) {
		return add(new EqualsTo(repository, path, value));
	}
	
	/**
	 * Adds a new {@link GreaterThan} test to {@link #tests} and returns it
	 * @param path
	 * @param value
	 * @return
	 * @category simple
	 */
	public <ComparableType extends Comparable<ComparableType>> VertexTest greaterThan(Iterable<Property> path, ComparableType value, boolean strictly) {
		return add(new GreaterThan<ComparableType>(repository, path, value, strictly));
	}

	/**
	 * Adds a new {@link LowerThan} test to {@link #tests} and returns it
	 * @param path
	 * @param value
	 * @return
	 * @category simple
	 */
	@Override
	public <ComparableType extends Comparable<ComparableType>> VertexTest lowerThan(Iterable<Property> path, ComparableType value, boolean strictly) {
		return add(new LowerThan<ComparableType>(repository, path, value, strictly));
	}

	@Override
	public VertexTest containsString(Iterable<Property> path, String contained) {
		return add(new ContainsString(repository, path, contained));
	}

	@Override
	public VertexTest startsWith(Iterable<Property> path, String start) {
		return add(new StartsWith(repository, path, start));
	}

	@Override
	public VertexTest endsWith(Iterable<Property> path, String end) {
		return add(new EndsWith(repository, path, end));
	}

	@Override
	public VertexTest collectionContains(Iterable<Property> path, Object contained) {
		return add(new CollectionContains(repository, path, contained));
	}

	@Override
	public VertexTest mapContainsValue(Iterable<Property> path, Object contained) {
		return add(new MapContainsValue(repository, path, contained));
	}

	@Override
	public VertexTest mapContainsKey(Iterable<Property> path, Object contained) {
		return add(new MapContainsKey(repository, path, contained));
	}

	@Override
	public VertexTest anything(Iterable<Property> path) {
		return add(new Anything(repository, path));
	}
}
