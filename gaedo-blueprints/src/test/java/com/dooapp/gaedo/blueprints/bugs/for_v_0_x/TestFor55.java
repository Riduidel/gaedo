package com.dooapp.gaedo.blueprints.bugs.for_v_0_x;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.finders.FindPostByAuthor;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor55 extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor55.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor55(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	/**
	 * Test for http://gaedo.origo.ethz.ch/issues/55
	 */
	@Test
	public void searchPostByAuthorObject() {
		int postsOf  = getPostService().find().matching(new FindPostByAuthor(author)).count();
		// All posts are from the same author
		// notice there are the 3 main posts, AND the about page (which is a post)
		assertThat(postsOf, Is.is(4));
	}

	@Test
	public void ensureAuthorHasWrittenThosePosts() {
		assertThat(getPostService().find().matching(new FindPostByAuthor(author)).count(), IsNot.not(0));
	}
}
