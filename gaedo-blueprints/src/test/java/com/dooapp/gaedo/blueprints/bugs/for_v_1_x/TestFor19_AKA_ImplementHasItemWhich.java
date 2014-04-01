package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@Ignore
@RunWith(Parameterized.class)
public class TestFor19_AKA_ImplementHasItemWhich extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor19_AKA_ImplementHasItemWhich.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor19_AKA_ImplementHasItemWhich(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}
/*
	@Test
	public void can_find_tag_from_start() {
			Collection<Post>  found = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

				@Override
				public QueryExpression createMatchingExpression(PostInformer informer) {
					return informer.getTags().hasItemWhich
				}
			}).getAll());
			assertThat(found, IsCollectionContaining.hasItems(post1, post2, post3));
	}
*/
}
