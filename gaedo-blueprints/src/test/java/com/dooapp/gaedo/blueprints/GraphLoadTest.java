package com.dooapp.gaedo.blueprints;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.StringContains;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;

import static org.junit.Assert.assertThat;

@Ignore
@RunWith(Parameterized.class)
public class GraphLoadTest extends AbstractGraphTest {
	
	private static final Logger logger = Logger.getLogger(GraphLoadTest.class.getName());
	
	private long instanceCount;
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return TestUtils.loadTest();
	}
	
	public GraphLoadTest(AbstractGraphEnvironment<?> environment, long instanceCount) {
		super(environment);
		this.instanceCount = instanceCount;
	}

	@Test
	public void loadDbWithABunchOfTags() {
		logger.info("loading DB with "+instanceCount+" tags");
		for(long index = 1l; index < instanceCount; index++) {
			final Tag a = new Tag(TestUtils.A+index).withId(128+index);
			getTagService().create(a);
		}
		logger.info("now doing some brute-force scanning on "+instanceCount+" tags");
		Iterable<Tag> values = getTagService().find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.getText().startsWith(TestUtils.A+(instanceCount/2));
					}
				}).getAll();
		logger.info("doing something with the found tags in "+instanceCount+" tags");
		for(Tag t : values) {
			assertThat(t.getText(), StringContains.containsString(TestUtils.A));
		}
		
		logger.info("deleting "+instanceCount+" tags");
		for(long index = 1l; index < instanceCount; index++) {
			final Tag a = new Tag(TestUtils.A+index).withId(128+index);
			getTagService().delete(a);
		}
		logger.info("job's done for "+instanceCount+" tags");
	}
}
