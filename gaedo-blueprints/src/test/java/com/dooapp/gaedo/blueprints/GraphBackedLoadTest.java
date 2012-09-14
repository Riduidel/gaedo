package com.dooapp.gaedo.blueprints;

import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.sail.AbstractSailGraphTest;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;

import static org.junit.Assert.assertThat;
import static com.dooapp.gaedo.blueprints.TestUtils.*;

@Ignore
@RunWith(Parameterized.class)
public class GraphBackedLoadTest extends AbstractSailGraphTest {
	
	private static final Logger logger = Logger.getLogger(GraphBackedLoadTest.class.getName());
	
	private FinderCrudService<Tag, TagInformer> tagService;
	private long instanceCount;
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return TestUtils.loadTest();
	}
	
	public GraphBackedLoadTest(GraphProvider graph, long instanceCount) {
		super(graph);
		this.instanceCount = instanceCount;
	}

	@Before
	public void loadService() throws Exception {
		super.loadService();
		// Now add some services
		repository.add(createServiceFor(Tag.class, TagInformer.class));
		repository.add(createServiceFor(Post.class, PostInformer.class));
		tagService = repository.get(Tag.class);
	}
	
	@Test
	public void loadDbWithABunchOfTags() {
		logger.info("loading DB with "+instanceCount+" tags");
		for(long index = 1l; index < instanceCount; index++) {
			final Tag a = new Tag(A+index).withId(128+index);
			tagService.create(a);
		}
		logger.info("now doing some brute-force scanning on "+instanceCount+" tags");
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.getText().startsWith(A+(instanceCount/2));
					}
				}).getAll();
		logger.info("doing something with the found tags in "+instanceCount+" tags");
		for(Tag t : values) {
			assertThat(t.getText(), StringContains.containsString(A));
		}
		
		logger.info("deleting "+instanceCount+" tags");
		for(long index = 1l; index < instanceCount; index++) {
			final Tag a = new Tag(A+index).withId(128+index);
			tagService.delete(a);
		}
		logger.info("job's done for "+instanceCount+" tags");
	}
}
