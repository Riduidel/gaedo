package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.hamcrest.number.IsGreaterThan;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor73_AKA_SomeSearchMayFailWithPropertyGraph extends AbstractGraphPostSubClassTest {
	private static final Logger logger = Logger.getLogger(TestFor73_AKA_SomeSearchMayFailWithPropertyGraph.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor73_AKA_SomeSearchMayFailWithPropertyGraph(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void lookupUsingAnythingWork() {
		List<Post> posts = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getNote().isAnything();
			}
		}).getAll());
		// 3 posts and the about one
		assertThat(posts.size(), new IsGreaterThan<Integer>(0));
	}

	@Test
	public void lookupUsingGreaterThanWork() {
		List<Post> posts = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getNote().greaterThan(-1d);
			}
		}).getAll());
		// 3 posts and the about one
		assertThat(posts.size(), new IsGreaterThan<Integer>(0));
	}

	@Test
	public void lookupUsingLowerThanWork() {
		List<Post> posts = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getNote().lowerThan(10d);
			}
		}).getAll());
		// 3 posts and the about one
		assertThat(posts.size(), new IsGreaterThan<Integer>(0));
	}
}
