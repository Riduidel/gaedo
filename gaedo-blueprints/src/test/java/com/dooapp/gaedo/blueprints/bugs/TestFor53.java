package com.dooapp.gaedo.blueprints.bugs;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor53 extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor53.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor53(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	/**
	 * According to latest modifications, the both note and text will be linked to literal vertex containing value "3.0". How will it work ?
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void ensurePostscanBeSortedByNoteDescending() {
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
