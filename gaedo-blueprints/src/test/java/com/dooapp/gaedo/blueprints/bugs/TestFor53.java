package com.dooapp.gaedo.blueprints.bugs;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.blueprints.finders.FindPostByText;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

import static org.junit.Assert.assertThat;
import static com.dooapp.gaedo.blueprints.TestUtils.A;
import static com.dooapp.gaedo.blueprints.TestUtils.ABOUT_ID;
import static com.dooapp.gaedo.blueprints.TestUtils.ID_POST_1;
import static com.dooapp.gaedo.blueprints.TestUtils.LOGIN_FOR_UPDATE_ON_CREATE;
import static com.dooapp.gaedo.blueprints.TestUtils.SOME_NEW_TEXT;
import static com.dooapp.gaedo.blueprints.TestUtils.TEST_TAG_FOR_CREATE_ON_UPDATE;
import static com.dooapp.gaedo.blueprints.TestUtils.USER_LOGIN;
import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

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
