package com.dooapp.gaedo.blueprints.bugs;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor09 extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor09.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor09(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	/**
	 * According to latest modifications, the both note and text will be linked to literal vertex containing value "3.0". How will it work ?
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void ensurePostscanBeSortedByNoteDescending() throws IOException, ClassNotFoundException {
		Collection<Post> postsByAuthor= CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().equalsTo(author);
			}
		}).sortBy(new SortingBuilder<PostInformer>() {
			
			@Override
			public SortingExpression createSortingExpression(PostInformer informer) {
				return SortingExpression.Build.sort().withDescending(informer.getNote());
			}
		}).getAll());
		assertThat(postsByAuthor, IsCollectionContaining.hasItems(post1, post2, post3));
		float note = Float.MAX_VALUE;
		for(Post p : postsByAuthor) {
			assertThat(p.note<=note, Is.is(true));
			note = p.note;
		}
	}
}
