package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor63_AKA_SearchValuesBypassGraphLoading extends AbstractGraphPostSubClassTest {
	private static interface InformerTester<InformerType extends Informer<?>> {

		void test(InformerType informer);

	}
	private static final Logger logger = Logger.getLogger(TestFor63_AKA_SearchValuesBypassGraphLoading.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor63_AKA_SearchValuesBypassGraphLoading(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void searchingByAuthorDontLoadAuthor() {
		Post result = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().equalsTo(author);
			}
		}).getFirst();
		// this will always be Ok, except bug bugs (Is.is do an Object#equals(Object) check)
		assertThat(result.author, Is.is(author));
		// This is the real test
		assertThat(result.author, IsSame.sameInstance(author));
	}
}
