package com.dooapp.gaedo.blueprints.indexable;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.Neo4j;
import com.dooapp.gaedo.blueprints.Tinker;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.LazyInterfaceInformerLocator;
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
import com.dooapp.gaedo.test.beans.specific.Theme;
import com.dooapp.gaedo.test.beans.specific.ThemeInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

@RunWith(Parameterized.class)
public class GraphBackedPostSubClassFinderServiceTest {
	
	public static interface PostSubClassInformer extends Informer<PostSubClass> {
		
	}
	public static class PostSubClass extends Post {

		public PostSubClass() {
			super();
		}

		public PostSubClass(long id, String text, float note, State state, User author) {
			super(id, text, note, state, author);
		}

		public PostSubClass(long i, String string, int j, State public1, User author2, Map<String, String> theseMappings) {
			super(i, string, j, public1, author2, theseMappings);
		}
		
		
	}
	
	private static final Logger logger = Logger.getLogger(GraphBackedPostSubClassFinderServiceTest.class.getName());
	
	private static final String SOME_NEW_TEXT = "some new text";
	private static final String TAG_TEXT = "tag text";
	private static final String LOGIN_FOR_UPDATE_ON_CREATE = "login for update on create";
	private static final String TEST_TAG_FOR_CREATE_ON_UPDATE = "test tag for create on update";

	@Parameters
	public static Collection<Object[]> parameters() {
		Collection<Object[]> returned = new LinkedList<Object[]>();
		returned.add(new Object[] { "tinkergraph", new Tinker()});
//		returned.add(new Object[] { "orientgraph", new OrientDB()});
		returned.add(new Object[] { "neo4jgraph", new Neo4j()});
		return returned;
	}
	private static final long ID_POST_1 = 1001;
	private static final long ABOUT_ID = 10;
	private static final String USER_PASSWORD = "user password";
	private static final String USER_LOGIN = "user login";
	
	private static final String GRAPH_DIR = System.getProperty("user.dir")+"/target/tests/graph";
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private IndexableGraph graph;
	private SimpleServiceRepository repository;
	private String name;
	private GraphProvider graphProvider;
	private FinderCrudService<Tag, TagInformer> tagService;
	private FinderCrudService<Post, PostInformer> postService;
	private FinderCrudService<PostSubClass, PostSubClassInformer> postSubService;
	private FinderCrudService<User, UserInformer> userService;
	private User author;
	private Post post1;
	private Post post2;
	private Post post3;
	private Tag tag1;
	
	public GraphBackedPostSubClassFinderServiceTest(String name, GraphProvider graph) {
		this.name = name;
		this.graphProvider = graph;
	}

	@Before
	public void loadService() throws MalformedURLException {
		repository = new SimpleServiceRepository();
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		locator.add(new LazyInterfaceInformerLocator());
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		InformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);
		
		graph = graphProvider.get(GraphProvider.GRAPH_DIR+"/indexable");
		// Now add some services
		repository.add(new IndexableGraphBackedFinderService(Tag.class, TagInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new IndexableGraphBackedFinderService(Post.class, PostInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new IndexableGraphBackedFinderService(PostSubClass.class, PostSubClassInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new IndexableGraphBackedFinderService(User.class, UserInformer.class, proxyInformerFactory, repository, provider, graph));
		repository.add(new IndexableGraphBackedFinderService(Theme.class, ThemeInformer.class, proxyInformerFactory, repository, provider, graph));
		tagService = repository.get(Tag.class);
		postService = repository.get(Post.class);
		userService = repository.get(User.class);

		// create some objects
		author = new User().withId(1).withLogin(USER_LOGIN).withPassword(USER_PASSWORD);
		author.about = new Post(ABOUT_ID, "a message about that user", 5, State.PUBLIC, author);
		author = userService.create(author);
		tag1 = tagService.create(new Tag(1, TAG_TEXT));
	}
	
	private Map<String, String> theseMappings(String...strings) {
		Map<String, String> returned = new TreeMap<String, String>();
		for (int i = 0; i < strings.length; i++) {
			if(i+1<strings.length) {
				returned.put(strings[i++], strings[i]);
			}
		}
		return returned;
	}

	@After
	public void unload() {
//		userService.delete(author);
//		postService.delete(post1);
//		postService.delete(post2);
//		postService.delete(post3);
		graph.shutdown();
		File f = new File(GraphProvider.GRAPH_DIR);
		f.delete();
	}

	@Test 
	public void ensurePostSubClassServiceWorksWell() {
		final String METHOD_NAME = "ensurePostSubClassServiceWorksWell";
		PostSubClass newOne = new PostSubClass(ID_POST_1, METHOD_NAME,1.0f, State.PUBLIC, author);
		Post saved = postService.create(newOne);
		assertThat(saved, Is.is(PostSubClass.class));
		Post loaded = postService.find().matching(new QueryBuilder<PostInformer>() {
			
			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getText().equalsTo(METHOD_NAME);
			}
		}).getFirst();
		assertThat(loaded, Is.is(PostSubClass.class));
		if(postService instanceof IdBasedService) {
			Post fromId = ((IdBasedService<Post>) postService).findById(loaded.id);
			assertThat(fromId, Is.is(PostSubClass.class));
		}
	}

}
