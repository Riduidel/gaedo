package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.NoReturnableVertexException;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

/**
 * When graph is empty, no exception should be unexpected
 * @author ndx
 *
 */
@RunWith(Parameterized.class)
public class TestThatSearchOnEmptyGraphWork extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestThatSearchOnEmptyGraphWork.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestThatSearchOnEmptyGraphWork(AbstractGraphEnvironment<?> environment) {
		super(environment);
		// graph should be left empty for that very test
		withObjectsAlreadyLoaded = false;
	}

	@Test(expected=NoReturnableVertexException.class)
	public void ensureASearchForOneWontFailWhenGraphIsEmpty() {
		Post neverFound= getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				// the precise thing issue #53 result into
				return informer.getAuthor().equalsTo(null);
			}
		}).getFirst();
		assertThat(neverFound, IsNull.nullValue());
	}

	@Test
	public void ensureASearchForPostsWontFailWhenGraphIsEmpty() {
		Collection<Post> postsByAuthor= CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				// the precise thing issue #53 result into
				return informer.getAuthor().equalsTo(null);
			}
		}).getAll());
		assertThat(postsByAuthor.size(), Is.is(0));
	}
}
