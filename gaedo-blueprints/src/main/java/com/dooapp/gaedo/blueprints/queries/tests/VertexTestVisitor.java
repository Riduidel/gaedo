package com.dooapp.gaedo.blueprints.queries.tests;

import com.dooapp.gaedo.patterns.Visitor;

/**
 * Base interface for visitors of {@link VertexTest}
 * @author ndx
 *
 */
public interface VertexTestVisitor extends Visitor {

	/**
	 * Start visiting an and compound vertex. Notice {@link #endVisit(AndVertexTest)} should always been called
	 * @param andVertexTest
	 * @return true if visit should dive into that "and" tests
	 * @category compound
	 */
	boolean startVisit(AndVertexTest andVertexTest);

	/**
	 * @category compound
	 * @param andVertexTest
	 */
	void endVisit(AndVertexTest andVertexTest);

	/**
	 * Start visiting an or compound vertex. Notice {@link #endVisit(AndVertexTest)} should always been called
	 * @param orVertexTest
	 * @return TODO
	 * @return true if visit should dive into that "or" tests
	 * @category compound
	 */
	boolean startVisit(OrVertexTest orVertexTest);

	/**
	 * @category compound
	 * @param orVertexTest
	 */
	void endVisit(OrVertexTest orVertexTest);

	/**
	 * Start visiting an not compound vertex. Notice {@link #endVisit(AndVertexTest)} should always been called
	 * @param notVertexTest
	 * @return TODO
	 * @return true if visit should dive into that "not" tests
	 * @category compound
	 */
	boolean startVisit(NotVertexTest notVertexTest);

	/**
	 * @category compound
	 * @param notVertexTest
	 */
	void endVisit(NotVertexTest notVertexTest);

	void visit(Anything anything);

	<ComparableType extends Comparable<ComparableType>>void visit(LowerThan<ComparableType> anything);

	<ComparableType extends Comparable<ComparableType>>void visit(ComparableValuedVertexTest<ComparableType> anything);

	void visit(EqualsToIgnoreCase equalsToIgnoreCase);

	void visit(Matches equalsToIgnoreCase);

	void visit(StartsWith startsWith);

	void visit(MapContainsValue mapContainsValue);

	void visit(MapContainsKey mapContainsKey);

	void visit(CollectionContains collectionContains);

	void visit(ContainsString containsString);

	void visit(EndsWith endsWith);

	void visit(EqualsTo equalsTo);

	void visit(VertexPropertyTest vertexPropertyTest);

	void visit(InstanceOf instanceOf);


}
