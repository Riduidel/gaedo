package com.dooapp.gaedo.blueprints;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.pgm.Vertex;

import static com.dooapp.gaedo.blueprints.TestUtils.ABOUT_ID;
import static com.dooapp.gaedo.blueprints.TestUtils.ID_POST_1;
import static com.dooapp.gaedo.blueprints.TestUtils.TAG_TEXT;
import static com.dooapp.gaedo.blueprints.TestUtils.USER_LOGIN;
import static com.dooapp.gaedo.blueprints.TestUtils.USER_PASSWORD;

/**
 * Base class for post tests, allows some separation of concerns
 * @author ndx
 *
 */
public abstract class AbstractGraphPostTest extends AbstractGraphTest {

	protected User author;
	protected Post post1;
	protected Post post2;
	protected Post post3;
	protected Tag tag1;

	public AbstractGraphPostTest(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Before
	public void loadService() throws Exception {
		super.loadService();
		// Now add some services
	
		// create some objects
		author = new User().withId(1).withLogin(USER_LOGIN).withPassword(USER_PASSWORD);
		author.about = new Post(ABOUT_ID, "a message about that user", 5, State.PUBLIC, author);
		author = getUserService().create(author);
		post1 = getPostService().create(new Post(ID_POST_1, "post text for 1", 1, State.PUBLIC, author, theseMappings("a", "b")));
		post2 = getPostService().create(new Post(2, "post text for 2", 2, State.PUBLIC, author));
		post3 = getPostService().create(new Post(3, "post text for 3", 3, State.PUBLIC, author));
		tag1 = getTagService().create(new Tag(1, TAG_TEXT));
		author.posts.add(post1);
		author.posts.add(post2);
		author.posts.add(post3);
		author = getUserService().update(author);
	}

	protected Vertex findVertexIn(FinderCrudService<User, UserInformer> userService, User author) {
		if(environment.getGraph() instanceof TransactionalGraph) {
			((TransactionalGraph) environment.getGraph()).startTransaction();
		}
		try {
			return ((AbstractBluePrintsBackedFinderService) getUserService()).getIdVertexFor(author, false);
		} finally {
			if(environment.getGraph() instanceof TransactionalGraph) {
				((TransactionalGraph) environment.getGraph()).stopTransaction(Conclusion.SUCCESS);
			}
		}
	}

	public static Map<String, String> theseMappings(String...strings) {
		Map<String, String> returned = new TreeMap<String, String>();
		for (int i = 0; i < strings.length; i++) {
			if(i+1<strings.length) {
				returned.put(strings[i++], strings[i]);
			}
		}
		return returned;
	}

}
