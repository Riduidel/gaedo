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
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static com.dooapp.gaedo.utils.CollectionUtils.asList;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor43 extends AbstractGraphPostSubClassTest {
	private static interface InformerTester<InformerType extends Informer<?>> {

		void test(InformerType informer);
		
	}
	private static final Logger logger = Logger.getLogger(TestFor43.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor43(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test 
	public void ensureMapvaluesUpdatesCascadeWellForBug43() {
		final String METHOD_NAME = "ensureMapvaluesUpdatesCascadeWellForBug";
		PostSubClass newOne = new PostSubClass(0, METHOD_NAME,1.0f, State.PUBLIC, author);
		newOne.state = State.PUBLIC;
		newOne.anotherState = PostSubClass.AnotherStateForBug26.PUBLIC;
		newOne.getPostPages().put(1, new PostSubClass(0, METHOD_NAME+"_page_1",1.0f, State.PUBLIC, author));
		PostSubClass saved = getPostSubService().create(newOne);
		assertThat(saved, Is.is(PostSubClass.class));
		saved.getPostPages().get(1).text+= " updated";
		String pageText = saved.getPostPages().get(1).text;
		saved = getPostSubService().update(saved);
		PostSubClass loaded = ((IdBasedService<PostSubClass>) getPostSubService()).findById(saved.id);
		assertThat(loaded.getPostPages().get(1).text, Is.is(pageText));
	}
}
