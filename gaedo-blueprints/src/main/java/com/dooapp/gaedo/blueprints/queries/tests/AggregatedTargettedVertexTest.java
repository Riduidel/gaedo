package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Collection;
import java.util.LinkedList;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;

public abstract class AggregatedTargettedVertexTest extends TargettedVertexTest implements CompoundVertexTest{

	protected Collection<VertexTest> tests = new LinkedList<VertexTest>();

	public AggregatedTargettedVertexTest(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> p) {
		super(strategy, driver, p);
	}

	public <Type extends VertexTest> Type add(Type test) {
		this.tests.add(test);
		return test;
	}
	
	public OrVertexTest or() {
		return add(new OrVertexTest(strategy, getDriver(), path));
	}
	
	public AndVertexTest and() {
		return add(new AndVertexTest(strategy, getDriver(), path));
	}
	
	public NotVertexTest not() {
		return add(new NotVertexTest(strategy, getDriver(), path));
	}

	/**
	 * Adds a new {@link EqualsTo} test to {@link #tests} and returns it
	 * @param path
	 * @param value
	 * @see com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest#equalsTo(com.dooapp.gaedo.properties.Property, java.lang.Object)
	 * @category simple
	 */
	public VertexTest equalsTo(Iterable<Property> path, Object value) {
		return add(new EqualsTo(strategy, getDriver(), path, value));
	}
	
	/**
	 * Adds a new {@link GreaterThan} test to {@link #tests} and returns it
	 * @param path
	 * @param value
	 * @return
	 * @category simple
	 */
	public <ComparableType extends Comparable<ComparableType>> VertexTest greaterThan(Iterable<Property> path, ComparableType value, boolean strictly) {
		return add(new GreaterThan<ComparableType>(strategy, getDriver(), path, value, strictly));
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
		return add(new LowerThan<ComparableType>(strategy, getDriver(), path, value, strictly));
	}

	@Override
	public VertexTest containsString(Iterable<Property> path, String contained) {
		return add(new ContainsString(strategy, getDriver(), path, contained));
	}

	@Override
	public VertexTest startsWith(Iterable<Property> path, String start) {
		return add(new StartsWith(strategy, getDriver(), path, start));
	}

	@Override
	public VertexTest endsWith(Iterable<Property> path, String end) {
		return add(new EndsWith(strategy, getDriver(), path, end));
	}

	@Override
	public VertexTest collectionContains(Iterable<Property> path, Object contained) {
		return add(new CollectionContains(strategy, getDriver(), path, contained));
	}

	@Override
	public VertexTest mapContainsValue(Iterable<Property> path, Object contained) {
		return add(new MapContainsValue(strategy, getDriver(), path, contained));
	}

	@Override
	public VertexTest mapContainsKey(Iterable<Property> path, Object contained) {
		return add(new MapContainsKey(strategy, getDriver(), path, contained));
	}

	@Override
	public VertexTest anything(Iterable<Property> path) {
		return add(new Anything(strategy, getDriver(), path));
	}

	@Override
	protected StringBuilder toString(int deepness, StringBuilder builder) {
		StringBuilder returned = super.toString(deepness, builder).append("\n");
		for(VertexTest v : tests) {
			if(v instanceof TargettedVertexTest) {
				returned = ((TargettedVertexTest) v).toString(deepness+1, returned);
			} else {
				returned.append("\nwhat ? a non targettted vertex test ? what the hell ?");
			}
		}
		return returned;
	}
}
