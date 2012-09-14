package com.dooapp.gaedo.blueprints.indexable;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

import static org.junit.Assert.assertThat;

@Ignore
@RunWith(Parameterized.class)
public class IndexableLoadTest extends AbstractIndexableGraphTest {
	
	private static final Logger logger = Logger.getLogger(IndexableLoadTest.class.getName());
	
	private FinderCrudService<Tag, TagInformer> tagService;
	private IndexableGraph graph;
	private SimpleServiceRepository repository;
	private String name;
	private GraphProvider graphProvider;
	private long instanceCount;
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return TestUtils.loadTest();
	}
	
	public IndexableLoadTest(GraphProvider graph, long instanceCount) {
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
	
	@After
	public void unload() {
		graph.shutdown();
		File f = new File(GraphProvider.GRAPH_DIR);
		f.delete();
	}

	@Test
	public void loadDbWithABunchOfTags() {
		logger.info("loading DB with "+instanceCount+" tags");
		for(long index = 1l; index < instanceCount; index++) {
			final Tag a = new Tag(TestUtils.A+index).withId(128+index);
			tagService.create(a);
		}
		logger.info("now doing some brute-force scanning on "+instanceCount+" tags");
		Iterable<Tag> values = tagService.find().matching(
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
			tagService.delete(a);
		}
		logger.info("job's done for "+instanceCount+" tags");
	}
}
