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
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.blueprints.providers.Neo4j;
import com.dooapp.gaedo.blueprints.providers.Tinker;
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

import static com.dooapp.gaedo.blueprints.TestUtils.*;


@RunWith(Parameterized.class)
public class IndexablePostSubClassFinderServiceTest extends AbstractIndexableGraphTest{
	
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
	
	private static final Logger logger = Logger.getLogger(IndexablePostSubClassFinderServiceTest.class.getName());
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}
	private FinderCrudService<Tag, TagInformer> tagService;
	private FinderCrudService<Post, PostInformer> postService;
	private FinderCrudService<PostSubClass, PostSubClassInformer> postSubService;
	private FinderCrudService<User, UserInformer> userService;
	private User author;
	private Post post1;
	private Post post2;
	private Post post3;
	private Tag tag1;
	
	public IndexablePostSubClassFinderServiceTest(GraphProvider graph) {
		super(graph);
	}

	@Before
	public void loadService() throws Exception {
		super.loadService();
		// Now add some services
		repository.add(createServiceFor(Tag.class, TagInformer.class));
		repository.add(createServiceFor(Post.class, PostInformer.class));
		repository.add(createServiceFor(PostSubClass.class, PostSubClassInformer.class));
		repository.add(createServiceFor(User.class, UserInformer.class));
		repository.add(createServiceFor(Theme.class, ThemeInformer.class));
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

	@Test 
	public void ensurePostSubClassServiceWorksWell() {
		final String METHOD_NAME = "ensurePostSubClassServiceWorksWell";
		PostSubClass newOne = new PostSubClass(1000+ID_POST_1, METHOD_NAME,1.0f, State.PUBLIC, author);
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
