package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static com.dooapp.gaedo.utils.CollectionUtils.asList;

import static org.junit.Assert.assertThat;
@RunWith(Parameterized.class)
public class TestFor47 extends AbstractGraphPostTest {
	private static interface InformerTester<InformerType extends Informer<?>> {

		void test(InformerType informer);
		
	}
	private static final Logger logger = Logger.getLogger(TestFor47.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor47(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	

	@Test
	public void testAuthorOfPost() {
		accessInformer(new InformerTester<PostInformer>() {

			@Override
			public void test(PostInformer informer) {
				assertThat(asList(informer.getAuthor().getFieldPath()).size(), Is.is(1));
				// a StringFieldInformer ? It's a simple case !
				assertThat(asList(informer.getAuthor().getLogin().getFieldPath()).size(), Is.is(2));
				// but another FieldInformer, here is the challenge
				assertThat(asList(informer.getAuthor().getTheme().getFieldPath()).size(), Is.is(2));
			}
			
		});
	}

	private void accessInformer(final InformerTester tester) {
		if(environment.getGraph() instanceof TransactionalGraph) {
			((TransactionalGraph) environment.getGraph()).startTransaction();
		}
		FinderCrudService<Post, PostInformer> service = getPostService();
		// Create a finder just to check some elements
		// Unfortunatly lazy evaluation requires to run the query (which will find no result)
		int count = service.find().matching(new QueryBuilder<PostInformer>() {
			
			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				tester.test(informer);
				return informer.getId().equalsTo(0l);
			}
		}).count();
		if(environment.getGraph() instanceof TransactionalGraph) {
			((TransactionalGraph) environment.getGraph()).stopTransaction(Conclusion.SUCCESS);
		}
	}
}
