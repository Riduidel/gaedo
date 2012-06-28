package com.dooapp.gaedo.blueprints;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
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
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

@Ignore
@RunWith(Parameterized.class)
public class MultithreadedGraphBackedTest {
	private class Work implements Runnable {

		private long index;

		public Work(long i) {
			this.index = i;
		}

		@Override
		public void run() {
			try {
				logger.info("working on "+index);
				Tag a = tagService.find().matching(new QueryBuilder<TagInformer>() {
	
					@Override
					public QueryExpression createMatchingExpression(TagInformer informer) {
						return informer.getText().equalsTo(A);
					}
				}).getFirst();
				assertThat(a.getText(), Is.is(A));
				
				Tag newOne = new Tag(A+"_"+index).withId(1000+index);
				newOne.parent = a;
				tagService.create(newOne);
				logger.info(index +" : tag created");
				// Now create a post linking to latest 10 tags created (but for that retrieve them)
				List<Tag> tags = new LinkedList<Tag>();
				for (long i = index; i > index-10 && i>0; i--) {
					final long value = i;
					tags.add(tagService.find().matching(new QueryBuilder<TagInformer>() {
	
						@Override
						public QueryExpression createMatchingExpression(TagInformer informer) {
							return informer.getText().equalsTo(A+"_"+value);
						}
					}).getFirst());
				}
				logger.info(index +" : tags list grabbed");
				Post toCreate = new Post(index, "a new post for "+index, 2.5f, State.PUBLIC, author);
				toCreate.tags.addAll(tags);
				postService.create(toCreate);
				logger.info(index +" : post created");
			} catch(RuntimeException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, index + " FFFFAILED :", e);
				}
			}
		}
		
	}
	
	private static final Logger logger = Logger.getLogger(MultithreadedGraphBackedTest.class.getName());
	
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

	private ExecutorService executorService;

	private FinderCrudService<Post, Informer<Post>> postService;

	private User author;
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Collection<Object[]> returned = new LinkedList<Object[]>();
//		returned.add(new Object[] { "tinkergraph", new Tinker()});
//		returned.add(new Object[] { "orientgraph", new OrientDB()});
		returned.add(new Object[] { "neo4jgraph", new Neo4j(),10l});
		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),10000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),100000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000000000l});
		return returned;
	}
	
	public MultithreadedGraphBackedTest(String name, GraphProvider graph, long instanceCount) {
		this.name = name;
		this.graphProvider = graph;
		this.instanceCount = instanceCount;
	}

	@Before
	public void loadService() throws MalformedURLException {
		executorService = Executors.newFixedThreadPool(50);
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
		repository.add(new BluePrintsBackedFinderService(Tag.class, TagInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new BluePrintsBackedFinderService(User.class, UserInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new BluePrintsBackedFinderService(Post.class, PostInformer.class, proxyInformerFactory, repository, provider, graph));
		tagService = repository.get(Tag.class);
		postService = repository.get(Post.class);
		tagService.create(new Tag(A).withId(1l));
		tagService.create(new Tag(B).withId(2l));
		FinderCrudService<User, UserInformer> userService = repository.get(User.class);
		author = userService.create(new User().withId(0).withLogin("test author").withPassword("a poassword"));
	}
	
	@After
	public void unload() throws InterruptedException {
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.DAYS);
		graph.shutdown();
		File f = new File(GRAPH_DIR);
		f.delete();
	}

	@Test
	public void loadDbWithABunchOfTags() {
		for (long i = 0; i < instanceCount; i++) {
			executorService.submit(new Work(i));
		}
	}
}
