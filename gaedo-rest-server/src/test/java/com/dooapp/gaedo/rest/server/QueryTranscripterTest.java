package com.dooapp.gaedo.rest.server;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
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
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.User;


public class QueryTranscripterTest {
	private static class FailingTestVisitor implements QueryExpressionVisitor {
		@Override
		public void visit(MapContainingKeyExpression mapContainingKeyExpression) {
			Assert.fail("there is no map expression here");
		}

		@Override
		public void visit(MapContainingValueExpression mapContainingValueExpression) {
			Assert.fail("there is no map expression here");
		}

		@Override
		public void visit(CollectionContaingExpression collectionContaingExpression) {
			Assert.fail("there is no collection expression here");
		}

		@Override
		public void visit(EndsWithExpression endsWithExpression) {
			Assert.fail("there is no ends with expression here");
		}

		@Override
		public void visit(StartsWithExpression startsWithExpression) {
			Assert.fail("there is no starts with expression here");
		}

		@Override
		public void visit(ContainsStringExpression containsStringExpression) {
			Assert.fail("there is no contains expression here");
		}

		@Override
		public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> lowerThanExpression) {
			Assert.fail("there is no lowerThan expression here");
		}

		@Override
		public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
			Assert.fail("there is no greaterThan expression here");
		}

		@Override
		public void visit(EqualsExpression expression) {
			Assert.fail("there is no equals expression here");
		}

		@Override
		public void startVisit(NotQueryExpression notQueryExpression) {
			Assert.fail("there is no NOT expression here");
		}

		@Override
		public void startVisit(AndQueryExpression andQueryExpression) {
			Assert.fail("there is no and expression here");
		}

		@Override
		public void startVisit(OrQueryExpression orQueryExpression) {
			Assert.fail("there is no or expression here");
		}

		@Override
		public void endVisit(NotQueryExpression notQueryExpression) {
			Assert.fail("there is no NOT expression here");
		}

		@Override
		public void endVisit(AndQueryExpression andQueryExpression) {
			Assert.fail("there is no and expression here");
		}

		@Override
		public void endVisit(OrQueryExpression orQueryExpression) {
			Assert.fail("there is no or expression here");
		}

		@Override
		public void visit(AnythingExpression anythingExpression) {
			Assert.fail("there is no anything expression here");
		}
	}

	private QueryTranscripter tested;
	private ServiceRepository repository;


	@Before
	public void prepare() {
		tested = new QueryTranscripter();
		repository = TestEnvironmentProvider.create();
	}

	@Test
	public void testBuildQueryForUser() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("filter[login][startsWith]", "fi");
		testData.put("filter[password][endsWith]", "d");

		QueryBuilder<Informer<User>> built =  tested.buildQuery(repository.get(User.class), testData);
		QueryExpression expression = built.createMatchingExpression(repository.get(User.class).getInformer());
		Assert.assertThat(expression, Is.is(AndQueryExpression.class));
		AndQueryExpression and = (AndQueryExpression) expression;
		and.accept(new FailingTestVisitor() {
			@Override
			public void visit(EndsWithExpression endsWithExpression) {
				Assert.assertThat(endsWithExpression.getEnd(), Is.is("d"));
				Assert.assertThat(endsWithExpression.getField(), Is.is(repository.get(User.class).getInformer().get("password").getField()));
			}
			
			@Override
			public void visit(StartsWithExpression startsWithExpression) {
				Assert.assertThat(startsWithExpression.getStart(), Is.is("fi"));
				Assert.assertThat(startsWithExpression.getField(), Is.is(repository.get(User.class).getInformer().get("login").getField()));
			}
			
			@Override
			public void startVisit(AndQueryExpression andQueryExpression) {
			}
			
			@Override
			public void endVisit(AndQueryExpression andQueryExpression) {
			}
		});
	}

	@Test
	public void testBuildQueryForPost() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("filter[text][contains]", "text");
		testData.put("filter[note][greaterThan]", "5");

		QueryBuilder<Informer<Post>> built =  tested.buildQuery(repository.get(Post.class), testData);
		QueryExpression expression = built.createMatchingExpression(repository.get(Post.class).getInformer());
		Assert.assertThat(expression, Is.is(AndQueryExpression.class));
		AndQueryExpression and = (AndQueryExpression) expression;
		and.accept(new FailingTestVisitor() {
			@Override
			public void visit(ContainsStringExpression containsStringExpression) {
				Assert.assertThat(containsStringExpression.getContained(), Is.is("text"));
				Assert.assertThat(containsStringExpression.getField(), Is.is(repository.get(Post.class).getInformer().get("text").getField()));
			}
			
			@Override
			public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
				Assert.assertThat(((Number) greaterThanExpression.getValue()).intValue(), Is.is(5));
				Assert.assertThat(greaterThanExpression.getField(), Is.is(repository.get(Post.class).getInformer().get("note").getField()));
			}
			@Override
			public void startVisit(AndQueryExpression andQueryExpression) {
			}
			
			@Override
			public void endVisit(AndQueryExpression andQueryExpression) {
			}
		});
	}
	

	@Test
	public void testBuildQueryForPostWithIncorrectField() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("filter[this field does not exists][contains]", "text");
		testData.put("filter[text][this method does not exists]", "text");
		testData.put("filter[note][greaterThan]", "5");

		QueryBuilder<Informer<Post>> built =  tested.buildQuery(repository.get(Post.class), testData);
		QueryExpression expression = built.createMatchingExpression(repository.get(Post.class).getInformer());
		Assert.assertThat(expression, Is.is(AndQueryExpression.class));
		AndQueryExpression and = (AndQueryExpression) expression;
		and.accept(new FailingTestVisitor() {
			@Override
			public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
				Assert.assertThat(((Number) greaterThanExpression.getValue()).intValue(), Is.is(5));
				Assert.assertThat(greaterThanExpression.getField(), Is.is(repository.get(Post.class).getInformer().get("note").getField()));
			}
			@Override
			public void startVisit(AndQueryExpression andQueryExpression) {
			}
			
			@Override
			public void endVisit(AndQueryExpression andQueryExpression) {
			}
		});
	}

	@Test
	public void testBuildQueryForPostWithOrStatement() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("filter["+FilterAggregator.OR.getKey()+"][text][startsWith]", "text");
		testData.put("filter["+FilterAggregator.OR.getKey()+"][note][greaterThan]", "5");

		QueryBuilder<Informer<Post>> built =  tested.buildQuery(repository.get(Post.class), testData);
		QueryExpression expression = built.createMatchingExpression(repository.get(Post.class).getInformer());
		Assert.assertThat(expression, Is.is(AndQueryExpression.class));
		AndQueryExpression and = (AndQueryExpression) expression;
		and.accept(new FailingTestVisitor() {
			@Override
			public void visit(StartsWithExpression startsWithExpression) {
				Assert.assertThat(startsWithExpression.getStart(), Is.is("text"));
				Assert.assertThat(startsWithExpression.getField(), Is.is(repository.get(Post.class).getInformer().get("text").getField()));
			}
			@Override
			public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> greaterThanExpression) {
				Assert.assertThat(((Number) greaterThanExpression.getValue()).intValue(), Is.is(5));
				Assert.assertThat(greaterThanExpression.getField(), Is.is(repository.get(Post.class).getInformer().get("note").getField()));
			}
			@Override
			public void startVisit(AndQueryExpression andQueryExpression) {
			}
			
			@Override
			public void endVisit(AndQueryExpression andQueryExpression) {
			}
			
			@Override
			public void startVisit(OrQueryExpression orQueryExpression) {
			}
			
			@Override
			public void endVisit(OrQueryExpression orQueryExpression) {
			}
		});
	}
}
