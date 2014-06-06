package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.GraphQueryStatement;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.OptimizedGraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.tests.EqualsTo;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTestVisitorAdapter;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsNot.not;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor79_AKA_ComplexVertexSearchHasItsPathRandomized extends AbstractGraphPostTest {
	private static class PathIsCorrect extends VertexTestVisitorAdapter {
		@Override
		public void visit(EqualsTo equalsTo) {
			Iterable<Property> path = equalsTo.getPath();
			Iterator<Property> pathIterator = path.iterator();
			assertThat(pathIterator.next().getName(), is("author"));
			assertThat(pathIterator.next().getName(), is("about"));
		}
	}
	private static final Logger logger = Logger.getLogger(TestFor79_AKA_ComplexVertexSearchHasItsPathRandomized.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor79_AKA_ComplexVertexSearchHasItsPathRandomized(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void can_search_post_which_author_has_the_good_about_message() {
		QueryStatement<Post,Post,PostInformer> finder = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().get("about").equalsTo(author.about);
			}

		});
		assertThat(finder, isA((Class) GraphQueryStatement.class));
		GraphQueryStatement<Post, Post, PostInformer> graphFinder = (GraphQueryStatement<Post, Post, PostInformer>) finder;
		GraphExecutableQuery query = graphFinder.prepareQuery();
		assertThat(query, isA((Class)OptimizedGraphExecutableQuery.class));
		OptimizedGraphExecutableQuery optimizedQuery = (OptimizedGraphExecutableQuery) query;
		optimizedQuery.getTest().accept(new PathIsCorrect());
		optimizedQuery.getExecutionPlan();
		optimizedQuery.getTest().accept(new PathIsCorrect());
	}
}
