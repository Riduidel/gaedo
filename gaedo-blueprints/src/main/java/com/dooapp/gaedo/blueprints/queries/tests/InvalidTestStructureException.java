package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Stack;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class InvalidTestStructureException extends BluePrintsCrudServiceException {

	public InvalidTestStructureException(Stack<CompoundVertexTest> tests) {
		super("there shold be only one test in test stack, but there are "+tests.size()+" please correct your test expression.");
	}

}
