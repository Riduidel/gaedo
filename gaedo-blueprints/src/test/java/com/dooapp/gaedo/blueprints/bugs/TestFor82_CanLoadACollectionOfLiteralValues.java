package com.dooapp.gaedo.blueprints.bugs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.finders.FindPostById;
import com.dooapp.gaedo.test.beans.Post;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor82_CanLoadACollectionOfLiteralValues extends AbstractGraphPostTest {
	public static class TestFor82_StringOnlyCollection extends Post {
		public Collection<String> extension = new ArrayList<String>();
		public TestFor82_StringOnlyCollection() {}
	}
	public static class TestFor82_ObjectCollection extends Post {
		public Collection<Serializable> extension = new ArrayList<Serializable>();
		public TestFor82_ObjectCollection() {}
	}
	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor82_CanLoadACollectionOfLiteralValues(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void a_collection_with_one_string_is_ok() {
		String METHOD_NAME = "a_collection_with_one_string_is_ok";
		TestFor82_StringOnlyCollection tested = new TestFor82_StringOnlyCollection();
		tested.text = METHOD_NAME;
		tested.extension.add(METHOD_NAME);
		tested = (TestFor82_StringOnlyCollection) getPostService().create(tested);
		// Now load it .. and hope the collection is correct
		tested = (TestFor82_StringOnlyCollection) getPostService().find().matching(new FindPostById(tested.id)).getFirst();
		assertThat(tested, instanceOf(TestFor82_StringOnlyCollection.class));
		assertThat(tested.extension, hasItem(METHOD_NAME));
	}

	@Test
	public void a_collection_with_one_string_and_one_object_is_ok() {
		String METHOD_NAME = "a_collection_with_one_string_is_ok";
		TestFor82_ObjectCollection tested = new TestFor82_ObjectCollection();
		tested.text = METHOD_NAME;
		tested.extension.add(METHOD_NAME);
		tested.extension.add(author);
		tested = (TestFor82_ObjectCollection) getPostService().create(tested);
		// Now load it .. and hope the collection is correct
		tested = (TestFor82_ObjectCollection) getPostService().find().matching(new FindPostById(tested.id)).getFirst();
		assertThat(tested, instanceOf(TestFor82_ObjectCollection.class));
		assertThat(tested.extension, Matchers.hasItems(METHOD_NAME, author));
	}
}
