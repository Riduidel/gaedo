package com.dooapp.gaedo.blueprints;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

@Ignore
@RunWith(Parameterized.class)
public class GraphBackedLoadTest {
	
	private static final Logger logger = Logger.getLogger(GraphBackedLoadTest.class.getName());
	
	static final String GRAPH_DIR = System.getProperty("user.dir")+"/target/tests/graph";
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private FinderCrudService<Tag, TagInformer> tagService;
	private IndexableGraph graph;
	private SimpleServiceRepository repository;
	private String name;
	private GraphProvider graphProvider;
	private long instanceCount;
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Collection<Object[]> returned = new LinkedList<Object[]>();
//		returned.add(new Object[] { "tinkergraph", new Tinker()});
//		returned.add(new Object[] { "orientgraph", new OrientDB()});
		returned.add(new Object[] { "neo4jgraph", new Neo4j(),10l});
		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000l});
		returned.add(new Object[] { "neo4jgraph", new Neo4j(),10000l});
		returned.add(new Object[] { "neo4jgraph", new Neo4j(),100000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000000000l});
		return returned;
	}
	
	public GraphBackedLoadTest(String name, GraphProvider graph, long instanceCount) {
		this.name = name;
		this.graphProvider = graph;
		this.instanceCount = instanceCount;
	}

	@Before
	public void loadService() throws MalformedURLException {
		repository = new SimpleServiceRepository();
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		InformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);
		
		graph = graphProvider.get();
		// Now add some services
		repository.add(new IndexableGraphBackedFinderService(Tag.class, TagInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new IndexableGraphBackedFinderService(Post.class, PostInformer.class, proxyInformerFactory, repository, provider, graph));
		tagService = repository.get(Tag.class);
	}
	
	@After
	public void unload() {
		graph.shutdown();
		File f = new File(GRAPH_DIR);
		f.delete();
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
