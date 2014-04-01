package com.dooapp.gaedo.blueprints.bugs.for_v_0_x;

import java.util.Collection;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.transformers.ClassLiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.core.IsNull.notNullValue;

import static org.junit.Assert.assertThat;

/**
 * As classes are no more separated vertices, this test has no more meaning
 * @author ndx
 *
 */
@Ignore
@RunWith(Parameterized.class)
public class TestFor69_AKA_ClassIsLoadedOnEachIteration extends AbstractGraphPostTest {
	private static ClassLiteralTransformer classTransformer = (ClassLiteralTransformer) Literals.classes.getTransformer();

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor69_AKA_ClassIsLoadedOnEachIteration(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	/**
	 * To make sure class is cached, we subclass the objectCache class with our creation and make sure it contains a key for User class as well as post one
	 */
	@Test
	public void loadSomePostsFast() {
		FinderCrudService<User, UserInformer> userService = getUserService();
		assertThat(userService, IsInstanceOf.instanceOf(IndexableGraphBackedFinderService.class));
		IndexableGraphBackedFinderService<User, UserInformer> userGraphService = (IndexableGraphBackedFinderService<User, UserInformer>) userService;
		ObjectCache testedCache = new ObjectCache();
		String authorId = userGraphService.getIdVertexId(author, false);
		userGraphService.loadObject(authorId, testedCache);
		// simple test ensuring cache worked ok
		assertThat(testedCache.get(authorId), notNullValue());
	}
}
