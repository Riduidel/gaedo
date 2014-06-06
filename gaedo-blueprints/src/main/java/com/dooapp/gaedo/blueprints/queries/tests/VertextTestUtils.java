package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Stack;

public class VertextTestUtils {
	private static class VertexTestRemover implements VertexTestVisitor {

		private VertexTest toIgnore;

		private Stack<CompoundVertexTest> tests = new Stack<CompoundVertexTest>();

		private CompoundVertexTest result;

		public VertexTestRemover(VertexTest toIgnore) {
			this.toIgnore = toIgnore;
		}

		public VertexTest removeFrom(VertexTest container) {
			container.accept(this);
			return result;
		}

		/**
		 * When adding a non comound test, operation is immediate : test is added to latest compound test in {@link #tests}.
		 * @param test
		 */
		private void add(VertexTest test) {
			tests.peek().add(test);
		}

		/**
		 * Adding a compound test is two fold : first it is pushed on stack, for its contained tests to be added through {@link #add(VertexTest)}.
		 * Then, once test has been fuly processed, the {@link #remove()} method is called.
		 * @param test
		 */
		private void add(CompoundVertexTest test) {
			tests.push(test);
		}

		/**
		 * Pop latest compound test and add it (if not empty) to parent.
		 */
		private void remove() {
			CompoundVertexTest latest = tests.pop();
			if (tests.isEmpty()) {
				result = latest;
			} else {
				if (!latest.getTests().isEmpty()) {
					tests.peek().add(latest);
				}
			}
		}

		@Override
		public boolean startVisit(AndVertexTest test) {
			if(!test.equals(toIgnore)) {
				add(new AndVertexTest(test.getStrategy(), test.getDriver(), test.getPath()));
			}
			return true;
		}

		@Override
		public void endVisit(AndVertexTest test) {
			remove();
		}

		@Override
		public boolean startVisit(OrVertexTest test) {
			if(!test.equals(toIgnore)) {
				add(new OrVertexTest(test.getStrategy(), test.getDriver(), test.getPath()));
			}
			return true;
		}

		@Override
		public void endVisit(OrVertexTest test) {
			remove();
		}

		@Override
		public boolean startVisit(NotVertexTest test) {
			if(!test.equals(toIgnore)) {
				add(new NotVertexTest(test.getStrategy(), test.getDriver(), test.getPath()));
			}
			return true;
		}

		@Override
		public void endVisit(NotVertexTest test) {
			remove();
		}

		@Override
		public void visit(Anything test) {
			if(!test.equals(toIgnore)) {
				add(new Anything(test.getStrategy(), test.getDriver(), test.getPath()));
			}
		}

		@Override
		public <ComparableType extends Comparable<ComparableType>> void visit(LowerThan<ComparableType> test) {
			if(!test.equals(toIgnore)) {
				add(new LowerThan(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected(), test.isStrictly()));
			}
		}

		@Override
		public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThan<ComparableType> test) {
			if(!test.equals(toIgnore)) {
				add(new GreaterThan(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected(), test.isStrictly()));
			}
		}

		@Override
		public void visit(EqualsToIgnoreCase test) {
			if(!test.equals(toIgnore)) {
				add(new EqualsToIgnoreCase(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(Matches test) {
			if(!test.equals(toIgnore)) {
				add(new Matches(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(StartsWith test) {
			if(!test.equals(toIgnore)) {
				add(new StartsWith(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(MapContainsValue test) {
			if(!test.equals(toIgnore)) {
				add(new MapContainsValue(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(MapContainsKey test) {
			if(!test.equals(toIgnore)) {
				add(new MapContainsKey(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(CollectionContains test) {
			if(!test.equals(toIgnore)) {
				add(new CollectionContains(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(ContainsString test) {
			if(!test.equals(toIgnore)) {
				add(new ContainsString(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(EndsWith test) {
			if(!test.equals(toIgnore)) {
				add(new EndsWith(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(EqualsTo test) {
			if(!test.equals(toIgnore)) {
				add(new EqualsTo(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

		@Override
		public void visit(VertexPropertyTest test) {
			if(!test.equals(toIgnore)) {
				add(new VertexPropertyTest(test.getStrategy(), test.getDriver(), test.getPath(), test.getPropertyName(), test.getExpected()));
			}
		}

		@Override
		public void visit(InstanceOf test) {
			if(!test.equals(toIgnore)) {
				add(new InstanceOf(test.getStrategy(), test.getDriver(), test.getPath(), test.getExpected()));
			}
		}

	}
	/**
	 * Create a copy of container test from which toIgnore (and all of its single containers) has been remove.
	 * @param container
	 * @param toIgnore
	 * @return
	 */
	public static VertexTest testWithout(VertexTest container, VertexTest toIgnore) {
		VertexTestRemover remover = new VertexTestRemover(toIgnore);
		return remover.removeFrom(container);
	}
}
