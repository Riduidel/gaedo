package com.dooapp.gaedo.blueprints.bugs.for_v_0_x;

import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.User;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

@RunWith(Parameterized.class)
public class TestFor33 extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor33.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor33(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}


	@Test
	public void deleteAPostWithNoAuthorShouldWorkForIssue33() {
		Post toDelete = getPostService().create(new Post(0, "deleteAPostWithNoAuthorShouldWork", 10.5f, State.PUBLIC, (User) null));
		getPostService().delete(toDelete);
	}
}
