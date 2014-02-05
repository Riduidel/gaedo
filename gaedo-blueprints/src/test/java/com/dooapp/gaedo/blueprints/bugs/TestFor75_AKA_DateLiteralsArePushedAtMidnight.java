package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.test.beans.Post;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor75_AKA_DateLiteralsArePushedAtMidnight extends AbstractGraphPostSubClassTest {
	private static final Logger logger = Logger.getLogger(TestFor75_AKA_DateLiteralsArePushedAtMidnight.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor75_AKA_DateLiteralsArePushedAtMidnight(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void post_publicationDate_can_be_set_to_8H30() {
		Post tested = getPostService().find().matching(new FindPostByNote(5.0)).getFirst();
		// set post time to 8:30
		tested.publicationDate.setHours(8);
		tested.publicationDate.setMinutes(30);
		getPostService().update(tested);
		// and now test
		Post loaded = getPostService().find().matching(new FindPostByNote(5.0)).getFirst();
		// just to make sure we didn't load a random post
		assertThat(loaded.id, Is.is(tested.id));
		// and now real test
		assertThat(loaded.publicationDate.getHours(), Is.is(8));
		assertThat(loaded.publicationDate.getMinutes(), Is.is(30));
	}
}
