package com.dooapp.gaedo.blueprints.bugs.for_v_0_x;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.projection.ProjectionBuilder;
import com.dooapp.gaedo.finders.projection.ValueFetcher;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor62_AKA_Projection extends AbstractGraphPostSubClassTest {
	private static interface InformerTester<InformerType extends Informer<?>> {

		void test(InformerType informer);

	}
	private static final Logger logger = Logger.getLogger(TestFor62_AKA_Projection.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor62_AKA_Projection(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void canProjectonSimpleFieldProvidedClassIsValid() {
		Iterable<String> result = getUserService().find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer informer) {
				return informer.getLogin().startsWith("u");
			}
		}).projectOn(new ProjectionBuilder<String, User, UserInformer>() {
			public String project(UserInformer informer, ValueFetcher fetcher) {
				return fetcher.getValue(informer.getLogin());
			}
		}).getAll();
		assertThat(CollectionUtils.asList(result).size(), IsNot.not(0));
		for(String s : result) {
			assertThat(s.startsWith("u"), Is.is(true));
		}
	}
}
