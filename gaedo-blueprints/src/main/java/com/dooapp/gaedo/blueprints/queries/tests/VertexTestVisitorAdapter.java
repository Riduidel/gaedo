package com.dooapp.gaedo.blueprints.queries.tests;

/**
 * An abstract adapter for that interface, allowing faster writing of effective visitors
 * @author ndx
 *
 */
public abstract class VertexTestVisitorAdapter implements VertexTestVisitor {

	@Override
	public boolean startVisit(AndVertexTest andVertexTest) {
		return true;
	}

	@Override
	public void endVisit(AndVertexTest andVertexTest) {
	}

	@Override
	public boolean startVisit(OrVertexTest orVertexTest) {
		return true;
	}

	@Override
	public void endVisit(OrVertexTest orVertexTest) {
	}

	@Override
	public boolean startVisit(NotVertexTest notVertexTest) {
		return true;
	}

	@Override
	public void endVisit(NotVertexTest notVertexTest) {
	}

	@Override
	public void visit(Anything anything) {
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThan<ComparableType> anything) {
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThan<ComparableType> anything) {
	}

	@Override
	public void visit(StartsWith startsWith) {
	}

	@Override
	public void visit(MapContainsValue mapContainsValue) {
	}

	@Override
	public void visit(MapContainsKey mapContainsKey) {
	}

	@Override
	public void visit(CollectionContains collectionContains) {
	}

	@Override
	public void visit(ContainsString containsString) {
	}

	@Override
	public void visit(EndsWith endsWith) {
	}

	@Override
	public void visit(EqualsTo equalsTo) {
	}

}
