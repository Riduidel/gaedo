package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.tinkerpop.blueprints.pgm.Vertex;

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
public class TestFor46 extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor46.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor46(AbstractGraphEnvironment<?> environment) {
		super(environment);
		// TODO Auto-generated constructor stub
	}

	
	@Test
	public void ensureSearchDoesntCreate() {
		String METHOD_NAME = "#ensureSearchDoesntCreate";
		final User author = new User();
		author.setLogin(METHOD_NAME);
		Vertex authorVertex = findVertexIn(getUserService(), author);
		assertThat(authorVertex, IsNull.nullValue());	
		int count = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().equalsTo(author);
			}
		}).count();
		assertThat(count, Is.is(0));
		assertThat(author.id, Is.is(0l));
		authorVertex = findVertexIn(getUserService(), author);
		assertThat(authorVertex, IsNull.nullValue());
	}
}
