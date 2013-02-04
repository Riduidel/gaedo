package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.NoReturnableVertexException;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.id.IdBasedService;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor48 extends AbstractGraphPostSubClassTest {
	private static interface InformerTester<InformerType extends Informer<?>> {

		void test(InformerType informer);
		
	}
	private static final Logger logger = Logger.getLogger(TestFor48.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor48(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void createAPostSubclassAndDeleteItWithPostService() {
		PostSubClass newOne = new PostSubClass();
		newOne = getPostSubService().create(newOne);
		long newId = newOne.id;
		getPostService().delete(newOne);
		newOne = null;
		// it should be deleted, no ?
		try {
			newOne = ((IdBasedService<PostSubClass>) getPostSubService()).findById(newId);
			assertThat(newOne, IsNull.nullValue());
		} catch(NoReturnableVertexException e) {
			// this is the normal result : vertex associated to PostSubClass should have been deleted now
		}
	}
}
