package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.finders.FindPostById;
import com.dooapp.gaedo.test.beans.Post;
import com.tinkerpop.blueprints.Vertex;

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
	public static class TestFor83_IntegerOnlyCollection extends Post {
		public Collection<Integer> extension = new ArrayList<Integer>();
		public TestFor83_IntegerOnlyCollection() {}
	}
	public static class TestFor83_BooleanOnlyCollection extends Post {
		public Collection<Boolean> extension = new ArrayList<Boolean>();
		public TestFor83_BooleanOnlyCollection() {}
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

	/**
	 * Specific test ensuring no NPE can be thrown
	 */
	@Test
	public void a_collection_with_one_string_and_one_object_but_no_size_property_is_ok() {
		String METHOD_NAME = "a_collection_with_one_string_is_ok";
		TestFor82_ObjectCollection tested = new TestFor82_ObjectCollection();
		tested.text = METHOD_NAME;
		tested.extension.add(METHOD_NAME);
		tested.extension.add(author);
		tested = (TestFor82_ObjectCollection) getPostService().create(tested);
		// before loading it, remove size property to emulate a "legacy"' value
		AbstractBluePrintsBackedFinderService<?, ?, ?> service = (AbstractBluePrintsBackedFinderService<?, ?, ?>) getPostService();
		Vertex value = service.getVertexFor(tested, CascadeType.REFRESH, new ObjectCache());
		// Remvoe all properties ending with size
		Collection<String> propertiesToRemove = new ArrayList<String>();
		for(String p : value.getPropertyKeys()) {
			if(p.endsWith(".size")) {
				propertiesToRemove.add(p);
			}
		}
		for(String s : propertiesToRemove) {
			value.removeProperty(s);
		}
		// Now load it .. and hope the collection is correct
		tested = (TestFor82_ObjectCollection) getPostService().find().matching(new FindPostById(tested.id)).getFirst();
		assertThat(tested, instanceOf(TestFor82_ObjectCollection.class));
		assertThat(tested.extension, Matchers.hasItems(METHOD_NAME, author));
	}

	/**
	 * Typical case exposed in bug 83
	 */
	@Test
	public void a_collection_with_integer_values_is_ok() {
		String METHOD_NAME = "a_collection_with_integer_values_is_ok";
		TestFor83_IntegerOnlyCollection tested = new TestFor83_IntegerOnlyCollection();
		tested.text = METHOD_NAME;
		tested.extension.add(2);
		tested.extension.add(5);
		tested = (TestFor83_IntegerOnlyCollection) getPostService().create(tested);
		// Now load it .. and hope the collection is correct
		tested = (TestFor83_IntegerOnlyCollection) getPostService().find().matching(new FindPostById(tested.id)).getFirst();
		assertThat(tested, instanceOf(TestFor83_IntegerOnlyCollection.class));
		assertThat(tested.extension, Matchers.hasItems(2,5));
	}

	/**
	 * Possible side effect of fixing 83 : boolean collections may be degraded
	 */
	@Test
	public void a_collection_with_boolean_values_is_ok() {
		String METHOD_NAME = "a_collection_with_boolean_values_is_ok";
		TestFor83_BooleanOnlyCollection tested = new TestFor83_BooleanOnlyCollection();
		tested.text = METHOD_NAME;
		tested.extension.add(true);
		tested.extension.add(false);
		tested = (TestFor83_BooleanOnlyCollection) getPostService().create(tested);
		// Now load it .. and hope the collection is correct
		tested = (TestFor83_BooleanOnlyCollection) getPostService().find().matching(new FindPostById(tested.id)).getFirst();
		assertThat(tested, instanceOf(TestFor83_BooleanOnlyCollection.class));
		assertThat(tested.extension, Matchers.hasItems(true, false));
	}

	/**
	 * I had an intuition about collection of Serializable containing mixed integers and booleans, and this intuition revealed true :
	 * when writing indexing properties for values 1 and 2, boolean values are overwritten.
	 */
//	@Test
	public void a_collection_mixing_booleans_and_integers_is_ok() {
		String METHOD_NAME = "a_collection_mixing_booleans_and_integers_is_ok";
		TestFor82_ObjectCollection tested = new TestFor82_ObjectCollection();
		tested.text = METHOD_NAME;
		tested.extension.add(true);
		tested.extension.add(false);
		tested.extension.add(1);
		tested.extension.add(2);
		tested = (TestFor82_ObjectCollection) getPostService().create(tested);
		// Now load it .. and hope the collection is correct
		tested = (TestFor82_ObjectCollection) getPostService().find().matching(new FindPostById(tested.id)).getFirst();
		assertThat(tested, instanceOf(TestFor82_ObjectCollection.class));
		assertThat(tested.extension, Matchers.hasItems((Serializable) true, false, 1, 2));
	}
}
