package com.dooapp.gaedo.blueprints.queries;

import java.util.Stack;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.OptimizedGraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.tests.AndVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.CompoundVertexTest;
import com.dooapp.gaedo.blueprints.queries.tests.InvalidTestStructureException;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.AnythingExpression;
import com.dooapp.gaedo.finders.expressions.CollectionContaingExpression;
import com.dooapp.gaedo.finders.expressions.ContainsStringExpression;
import com.dooapp.gaedo.finders.expressions.EndsWithExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.finders.expressions.GreaterThanExpression;
import com.dooapp.gaedo.finders.expressions.LowerThanExpression;
import com.dooapp.gaedo.finders.expressions.MapContainingKeyExpression;
import com.dooapp.gaedo.finders.expressions.NotQueryExpression;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;

/**
 * Visitor transforming the abstract {@link QueryExpression} into a more concrete and usable {@link GraphExecutableQuery}.
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public class BluePrintsQueryBuilder<DataType, InformerType extends Informer<DataType>> implements QueryExpressionVisitor {

	private AbstractBluePrintsBackedFinderService<?, DataType, InformerType> service;
	/**
	 * This stack contains only the tests allowing tree building
	 */
	private Stack<CompoundVertexTest> tests = new Stack<CompoundVertexTest>();

	public BluePrintsQueryBuilder(AbstractBluePrintsBackedFinderService<?, DataType, InformerType> service) {
		this.service = service;
		/* Base test is always a AND one, associated to a test on class (will be used for optimized query building) */
		this.tests.push(new AndVertexTest(service.getDriver(), null /* null indicates no property is navigated */));
	}

	/**
	 * Effectively creates the query object that will be used to browse the graph DB
	 * @param sortingExpression
	 * @return
	 */
	public GraphExecutableQuery getQuery(SortingExpression sortingExpression) {
		// At the end of the visit, there should be only one item in stack : the root one
		if(tests.size()!=1) {
			throw new InvalidTestStructureException(tests);
		}
		return new OptimizedGraphExecutableQuery(service, tests.peek(), sortingExpression);
//		return new BasicGraphExecutableQuery(service, tests.peek(), sortingExpression);
	}

	@Override
	public void visit(EqualsExpression expression) {
		tests.peek().equalsTo(expression.getFieldPath(), expression.getValue());
	}

	@Override
	public void startVisit(OrQueryExpression orQueryExpression) {
		tests.push(tests.peek().or());
	}

	@Override
	public void endVisit(OrQueryExpression orQueryExpression) {
		tests.pop();
	}

	@Override
	public void startVisit(AndQueryExpression andQueryExpression) {
		tests.push(tests.peek().and());
	}

	@Override
	public void endVisit(AndQueryExpression andQueryExpression) {
		tests.pop();
 	}

	@Override
	public void startVisit(NotQueryExpression notQueryExpression) {
		tests.push(tests.peek().not());
	}

	@Override
	public void endVisit(NotQueryExpression notQueryExpression) {
		tests.pop();
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
		tests.peek().greaterThan(greaterThanExpression.getFieldPath(), greaterThanExpression.getValue(), greaterThanExpression.isStrictly());
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> lowerThanExpression) {
		tests.peek().lowerThan(lowerThanExpression.getFieldPath(), lowerThanExpression.getValue(), lowerThanExpression.isStrictly());
	}

	@Override
	public void visit(ContainsStringExpression containsStringExpression) {
		tests.peek().containsString(containsStringExpression.getFieldPath(), containsStringExpression.getContained());
	}

	@Override
	public void visit(StartsWithExpression startsWithExpression) {
		tests.peek().startsWith(startsWithExpression.getFieldPath(), startsWithExpression.getStart());
	}

	@Override
	public void visit(EndsWithExpression endsWithExpression) {
		tests.peek().endsWith(endsWithExpression.getFieldPath(), endsWithExpression.getEnd());
	}

	@Override
	public void visit(CollectionContaingExpression collectionContaingExpression) {
		tests.peek().collectionContains(collectionContaingExpression.getFieldPath(), collectionContaingExpression.getContained());
	}

	@Override
	public void visit(MapContainingValueExpression mapContainingValueExpression) {
		tests.peek().mapContainsValue(mapContainingValueExpression.getFieldPath(), mapContainingValueExpression.getContained());
	}

	@Override
	public void visit(MapContainingKeyExpression mapContainingKeyExpression) {
		tests.peek().mapContainsKey(mapContainingKeyExpression.getFieldPath(), mapContainingKeyExpression.getContained());
	}

	@Override
	public void visit(AnythingExpression anythingExpression) {
		tests.peek().anything(anythingExpression.getFieldPath());
	}

}
