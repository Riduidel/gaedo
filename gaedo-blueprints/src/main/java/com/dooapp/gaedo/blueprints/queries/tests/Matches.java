package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.regex.Pattern;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.properties.Property;

public class Matches extends AbstractStringVertexTest<Pattern> implements VertexTest {

	public Matches(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> path, Pattern pattern) {
		super(strategy, driver, path, pattern);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean testString(String searched, Pattern expected) {
		return expected.matcher(searched).matches();
	}

}
