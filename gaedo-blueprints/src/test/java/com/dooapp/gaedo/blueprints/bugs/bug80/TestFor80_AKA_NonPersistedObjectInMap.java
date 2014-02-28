package com.dooapp.gaedo.blueprints.bugs.bug80;

import java.util.Collection;
import java.util.SortedSet;

import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor80_AKA_NonPersistedObjectInMap extends AbstractGraphPostTest {

	private final class FindById implements QueryBuilder<Bug80ValueInformer> {
		private final String id;

		private FindById(String id) {
			this.id = id;
		}

		@Override
		public QueryExpression createMatchingExpression(Bug80ValueInformer informer) {
			return informer.getText().equalsTo(id);
		}
	}

	private InViewService<Bug80Value, Bug80ValueInformer, SortedSet<String>> bug80ValueService;

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor80_AKA_NonPersistedObjectInMap(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Before
	public void loadService() throws Exception {
		super.loadService();
		bug80ValueService = environment.createServiceFor(Bug80Value.class, Bug80ValueInformer.class, environment.strategy);
	}

	@Test
	public void can_persist_a_value() {
		String METHOD_NAME = getClass().getName()+"#can_persist_a_value";
		Bug80Value loaded = bug80ValueService.create(new Bug80Value().withText(METHOD_NAME));
		assertThat(loaded.getId(), IsNull.notNullValue());
	}

	@Test
	public void can_perists_a_string_value_when_in_a_map() {
		final String METHOD_NAME = getClass().getName()+"#can_perists_a_string_value_when_in_a_map";
		Bug80Value loaded = bug80ValueService.create(new Bug80Value().withText(METHOD_NAME));
		assertThat(loaded.getId(), IsNull.notNullValue());
		// now come the hard part
		loaded.getElements().put("a", "b");
		loaded = bug80ValueService.update(loaded);
		loaded = bug80ValueService.find().matching(new FindById(METHOD_NAME)).getFirst();
		assertThat(loaded.getElements(), IsMapContaining.hasEntry("a",  (Object) "b"));
	}

	@Test
	public void can_perists_an_object_value_when_in_a_map() {
		final String METHOD_NAME = getClass().getName()+"#can_perists_an_object_value_when_in_a_map";
		Bug80Value loaded = bug80ValueService.create(new Bug80Value().withText(METHOD_NAME));
		assertThat(loaded.getId(), IsNull.notNullValue());
		// now come the hard part
		loaded.getElements().put("a", new Bug80Value().withText("in map"));
		loaded = bug80ValueService.update(loaded);
		loaded = bug80ValueService.find().matching(new FindById(METHOD_NAME)).getFirst();
		assertThat(loaded.getElements(), IsMapContaining.hasKey("a"));
		assertThat(loaded.getElements().get("a"), Is.isA((Class)Bug80Value.class));
	}
}
