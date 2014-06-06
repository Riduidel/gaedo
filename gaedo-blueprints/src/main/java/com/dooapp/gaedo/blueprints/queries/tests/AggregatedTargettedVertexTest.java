package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

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

	@Override
	public VertexTest instanceOf(Iterable<Property> path, Class<?> type) {
		return add(new InstanceOf(strategy, getDriver(), path, type));
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
	public VertexTest matches(Iterable<Property> path, Pattern pattern) {
		return add(new Matches(strategy, getDriver(), path, pattern));
	}

	@Override
	public VertexTest containsString(Iterable<Property> path, String contained) {
		return add(new ContainsString(strategy, getDriver(), path, contained));
	}

	@Override
	public VertexTest equalsToIgnoreCase(Iterable<Property> path, String compared) {
		return add(new EqualsToIgnoreCase(strategy, getDriver(), path, compared));
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

	/**
	 * @return the tests
	 * @category getter
	 * @category tests
	 */
	public Collection<VertexTest> getTests() {
		return tests;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((tests == null) ? 0 : tests.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AggregatedTargettedVertexTest other = (AggregatedTargettedVertexTest) obj;
		if (tests == null) {
			if (other.tests != null)
				return false;
		} else if (!tests.equals(other.tests))
			return false;
		return true;
	}
}
